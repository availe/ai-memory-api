package io.availe.memory

import io.availe.ai.EmbeddingService
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

    suspend fun ingestText(text: String): UUID {
        logger.info("Ingesting text...")
        return processContent(text, MemoryRepository.SourceType.TEXT, "{}")
    }

    suspend fun ingestFile(filename: String, content: ByteArray): UUID {
        logger.info("Ingesting file: {}", filename)

        val text = try {
            when {
                filename.endsWith(".pdf", ignoreCase = true) -> {
                    Loader.loadPDF(content).use { document ->
                        PDFTextStripper().getText(document)
                    }
                }

                else -> content.decodeToString()
            }
        } catch (e: Exception) {
            logger.error("Failed to extract text from file: $filename", e)
            throw IllegalArgumentException("Could not parse file: $filename", e)
        }

        if (text.isBlank()) {
            throw IllegalArgumentException("File $filename resulted in empty text content")
        }

        val metadata = mapOf("filename" to filename)
        return processContent(text, MemoryRepository.SourceType.FILE, Json.encodeToString(metadata))
    }

    private suspend fun processContent(
        text: String,
        sourceType: MemoryRepository.SourceType,
        metadata: String
    ): UUID {
        val embedding = embeddingService.embed(text)
        val similarMemories = memoryRepository.findSimilar(embedding, limit = 3, threshold = 0.85)
        val newMemoryId = memoryRepository.save(text, embedding, sourceType, metadata)

        similarMemories.forEach { similar ->
            logger.info("Auto-linking memory {} (score: {}) as EXTEND", similar.id, similar.score)
            memoryRepository.createEdge(similar.id, newMemoryId, MemoryRepository.RelationshipType.EXTEND)
        }

        return newMemoryId
    }
}