package io.availe.graph

import io.availe.memory.MemoryRepository

internal class GraphService(private val memoryRepository: MemoryRepository) {

    fun getGraphData(): GraphData {
        val memories = memoryRepository.findAllMemories()
        val edges = memoryRepository.findAllEdges()

        val nodes = memories.map { memory ->
            GraphNode(
                id = memory.id.toString(),
                label = memory.content.take(50) + if (memory.content.length > 50) "..." else "",
                type = memory.sourceType.name
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

        return GraphData(nodes, graphEdges)
    }
}