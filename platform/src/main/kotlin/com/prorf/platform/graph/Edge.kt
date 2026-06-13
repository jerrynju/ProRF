package com.prorf.platform.graph

/** Directed connection between two node ports in a workflow graph. */
data class Edge(
    val id: String,
    val fromNodeId: String,
    val fromPortId: String,
    val toNodeId: String,
    val toPortId: String,
)
