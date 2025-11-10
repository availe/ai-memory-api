package io.availe.graph

import io.availe.memory.MemoryRepository
import org.slf4j.LoggerFactory

internal class GraphService(private val memoryRepository: MemoryRepository) {
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
}