package com.prorf.ui.model

data class UiEdgeRow(
    val edgeId: String,
    val otherNodeLabel: String,
    val direction: EdgeDirection,
)

enum class EdgeDirection { INCOMING, OUTGOING }
