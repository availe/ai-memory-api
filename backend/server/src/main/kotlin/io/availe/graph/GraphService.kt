package io.availe.graph

import io.availe.ai.EmbeddingService
import io.availe.memory.MemoryRepository
import org.slf4j.LoggerFactory
import java.util.*

internal class GraphService(
    private val memoryRepository: MemoryRepository,
    private val embeddingService: EmbeddingService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getGraphData(): GraphData {
        logger.info("Fetching graph data from repository...")
        val memories = memoryRepository.findAllMemories()
        val edges = memoryRepository.findAllEdges()

        logger.info("Found ${memories.size} memories and ${edges.size} edges.")

        val nodes = memories.map { memory ->
            GraphNode(
                id = memory.id.toString(),
                label = memory.content.take(50) + if (memory.content.length > 50) "..." else "",
                type = memory.sourceType.name,
                fullContent = memory.content,
                metadata = memory.metadata.data(),
                createdAt = memory.createdAt.toString()
            )
        }

        val graphEdges = edges.map { edge ->
            GraphEdge(
                id = "${edge.sourceId}-${edge.targetId}",
                source = edge.sourceId.toString(),
                target = edge.targetId.toString(),
                label = edge.relationship.name
            )
        }

        logger.info("Returning GraphData with ${nodes.size} nodes and ${graphEdges.size} edges.")
        return GraphData(nodes, graphEdges)
    }

    suspend fun search(query: String): List<MemoryRepository.ScoredMemory> {
        logger.info("Searching for: '$query'")
        val embedding = embeddingService.embed(query)

        val results = memoryRepository.findSimilar(embedding, limit = 10, threshold = 0.50)

        logger.info(">>> DEBUG: Found ${results.size} results. Scores: ${results.map { it.score }}")

        return results
    }

    fun createEdge(sourceIdStr: String, targetIdStr: String, typeStr: String) {
        val sourceId = UUID.fromString(sourceIdStr)
        val targetId = UUID.fromString(targetIdStr)
        val type = try {
            MemoryRepository.RelationshipType.valueOf(typeStr.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid relationship type: $typeStr. Valid types are: ${MemoryRepository.RelationshipType.entries.joinToString()}")
        }

        logger.info("Creating edge: $sourceId -[$type]-> $targetId")
        memoryRepository.createEdge(sourceId, targetId, type)
    }
}