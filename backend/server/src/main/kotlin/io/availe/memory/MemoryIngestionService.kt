package io.availe.memory

import io.availe.ai.EmbeddingService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.slf4j.LoggerFactory
import java.util.*

internal class MemoryIngestionService(
    private val embeddingService: EmbeddingService,
    private val memoryRepository: MemoryRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun chunkText(
        text: String,
        maxChunkSize: Int = 1000,
        minChunkSize: Int = 20
    ): List<String> {
        val paragraphs = text.split("\n\n")
            .map { it.trim() }
            .filter { it.length > minChunkSize }

        val finalChunks = mutableListOf<String>()

        for (paragraph in paragraphs) {
            if (paragraph.length <= maxChunkSize) {
                finalChunks.add(paragraph)
            } else {
                finalChunks.addAll(
                    paragraph.chunked(maxChunkSize)
                        .map { it.trim() }
                        .filter { it.length > minChunkSize }
                )
            }
        }
        return finalChunks
    }


    suspend fun ingestText(text: String): UUID {
        logger.info("Ingesting text...")

        val normalizedText = text.replace(Regex("(\r\n|\r|\n){2,}"), "\n\n")
        val chunks = chunkText(normalizedText)

        if (chunks.isEmpty()) {
            if (text.length > 20) {
                return embedAndSaveChunk(text, MemoryRepository.SourceType.TEXT, "{}")
            }
            throw IllegalArgumentException("No ingest-able text provided.")
        }

        if (chunks.size == 1) {
            return embedAndSaveChunk(chunks.first(), MemoryRepository.SourceType.TEXT, "{}")
        }

        val parentMetadata = Json.encodeToString(mapOf("isParent" to "true"))
        val rootId = embedAndSaveChunk(
            "Text Ingest: ${text.take(50)}...",
            MemoryRepository.SourceType.TEXT,
            parentMetadata,
            false
        )

        coroutineScope {
            val chunkIds = chunks.map { chunk ->
                async {
                    val chunkMetadata = Json.encodeToString(mapOf("parent" to rootId.toString()))
                    embedAndSaveChunk(chunk, MemoryRepository.SourceType.TEXT, chunkMetadata, false)
                }
            }.awaitAll()

            chunkIds.forEach { chunkId ->
                memoryRepository.createEdge(rootId, chunkId, MemoryRepository.RelationshipType.BELONGS_TO)
            }
        }

        return rootId
    }

    suspend fun ingestFile(filename: String, content: ByteArray): UUID {
        logger.info("Ingesting file: {}", filename)

        val text = try {
            when {
                filename.endsWith(".pdf", ignoreCase = true) -> {
                    Loader.loadPDF(content).use { document ->
                        val stripper = PDFTextStripper()
                        stripper.sortByPosition = true
                        stripper.paragraphStart = "\n\n"
                        stripper.paragraphEnd = "\n"
                        stripper.getText(document)
                    }
                }

                else -> {
                    val rawText = content.decodeToString()
                    rawText.replace(Regex("(\r\n|\r|\n){2,}"), "\n\n")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to extract text from file: $filename", e)
            throw IllegalArgumentException("Could not parse file: $filename", e)
        }

        if (text.isBlank()) {
            throw IllegalArgumentException("File $filename resulted in empty text content")
        }

        val chunks = chunkText(text)
        val fileMetadata = Json.encodeToString(mapOf("filename" to filename))

        if (chunks.isEmpty()) {
            if (text.length > 20) {
                return embedAndSaveChunk(text, MemoryRepository.SourceType.FILE, fileMetadata)
            }
            throw IllegalArgumentException("File $filename did not produce any valid text chunks.")
        }

        if (chunks.size == 1) {
            return embedAndSaveChunk(chunks.first(), MemoryRepository.SourceType.FILE, fileMetadata)
        }

        val parentMetadata = Json.encodeToString(mapOf("filename" to filename, "isParent" to "true"))
        val rootId = embedAndSaveChunk(
            "File: $filename",
            MemoryRepository.SourceType.FILE,
            parentMetadata,
            false
        )

        coroutineScope {
            val chunkIds = chunks.map { chunk ->
                async {
                    val chunkMetadata = Json.encodeToString(
                        mapOf(
                            "filename" to filename,
                            "parent" to rootId.toString()
                        )
                    )
                    embedAndSaveChunk(chunk, MemoryRepository.SourceType.FILE, chunkMetadata, false)
                }
            }.awaitAll()

            chunkIds.forEach { chunkId ->
                memoryRepository.createEdge(rootId, chunkId, MemoryRepository.RelationshipType.BELONGS_TO)
            }
        }

        return rootId
    }

    private suspend fun embedAndSaveChunk(
        text: String,
        sourceType: MemoryRepository.SourceType,
        metadata: String,
        autoLink: Boolean = true
    ): UUID {
        val embedding = embeddingService.embed(text)
        val newMemoryId = memoryRepository.save(text, embedding, sourceType, metadata)

        if (autoLink) {
            val similarMemories = memoryRepository.findSimilar(embedding, limit = 3, threshold = 0.85)
            similarMemories.forEach { similar ->
                if (similar.id != newMemoryId) {
                    logger.info("Auto-linking memory {} (score: {}) as EXTEND", similar.id, similar.score)
                    memoryRepository.createEdge(similar.id, newMemoryId, MemoryRepository.RelationshipType.EXTEND)
                }
            }
        }

        return newMemoryId
    }
}