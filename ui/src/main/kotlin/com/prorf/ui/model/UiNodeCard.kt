package com.prorf.ui.model

import com.prorf.platform.graph.NodeInstance

/**
 * L2 UI model — view-layer representation of a node on the canvas.
 * Derives display state from a NodeInstance but adds UI concerns
 * (selection, position on canvas, last execution output preview).
 * Must never reference RF concepts directly.
 */
data class UiNodeCard(
    val nodeId: String,
    val typeId: String,
    val displayName: String,
    val x: Float,
    val y: Float,
    val isSelected: Boolean = false,
    val status: NodeStatus = NodeStatus.IDLE,
    val outputSummary: String = "",
)

enum class NodeStatus { IDLE, RUNNING, SUCCESS, ERROR }

fun NodeInstance.toUiCard(
    displayName: String,
    isSelected: Boolean = false,
    status: NodeStatus = NodeStatus.IDLE,
    outputSummary: String = "",
): UiNodeCard = UiNodeCard(
    nodeId = id,
    typeId = typeId,
    displayName = displayName,
    x = position.x,
    y = position.y,
    isSelected = isSelected,
    status = status,
    outputSummary = outputSummary,
)
