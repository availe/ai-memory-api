package io.availe.graph

import kotlinx.serialization.Serializable

@Serializable
data class GraphNode(
    val id: String,
    val label: String,
    val type: String,
    val fullContent: String,
    val metadata: String,
    val createdAt: String
)

@Serializable
data class GraphEdge(
    val id: String,
    val source: String,
    val target: String,
    val label: String
)

@Serializable
data class GraphData(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
)