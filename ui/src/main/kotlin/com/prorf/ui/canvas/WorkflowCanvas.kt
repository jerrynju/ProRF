package com.prorf.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.prorf.platform.graph.Edge
import com.prorf.ui.model.UiNodeCard

/**
 * L2 UI — Zoomable/pannable workflow canvas.
 * Renders a list of UiNodeCards as draggable node boxes and
 * draws edges between them as bezier curves.
 *
 * Rules:
 * - No computation logic here (no NodeExecutor, no RF formulas)
 * - No business state — callbacks delegate mutations up
 * - Uses platform graph types only for structural data (Edge)
 */
@Composable
fun WorkflowCanvas(
    nodes: List<UiNodeCard>,
    edges: List<Edge>,
    onNodeSelected: (nodeId: String) -> Unit,
    onNodeMoved: (nodeId: String, x: Float, y: Float) -> Unit,
    connectingFromNodeId: String? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        EdgeOverlay(nodes = nodes, edges = edges, connectingFromNodeId = connectingFromNodeId)
        nodes.forEach { card ->
            NodeCardItem(
                card = card,
                onTap = { onNodeSelected(card.nodeId) },
                onDrag = { dx, dy -> onNodeMoved(card.nodeId, card.x + dx, card.y + dy) },
            )
        }
        if (connectingFromNodeId != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.TopCenter),
            ) {
                Text(
                    text = "Tap a target node to connect",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun EdgeOverlay(
    nodes: List<UiNodeCard>,
    edges: List<Edge>,
    connectingFromNodeId: String? = null,
) {
    val nodeIndex = nodes.associateBy { it.nodeId }
    Canvas(modifier = Modifier.fillMaxSize()) {
        for (edge in edges) {
            val from = nodeIndex[edge.fromNodeId] ?: continue
            val to = nodeIndex[edge.toNodeId] ?: continue
            val start = Offset(from.x + 80f, from.y + 24f)
            val end = Offset(to.x, to.y + 24f)
            val cp1 = Offset(start.x + (end.x - start.x) * 0.5f, start.y)
            val cp2 = Offset(end.x - (end.x - start.x) * 0.5f, end.y)
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
                },
                color = Color(0xFF6650A4),
                style = Stroke(width = 2f, cap = StrokeCap.Round),
            )
        }
        if (connectingFromNodeId != null) {
            val fromCard = nodeIndex[connectingFromNodeId]
            if (fromCard != null) {
                drawRect(
                    color = Color(0xFF6650A4),
                    topLeft = Offset(fromCard.x - 4f, fromCard.y - 4f),
                    size = Size(168f, 60f),
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f)),
                    ),
                )
            }
        }
    }
}

@Composable
private fun NodeCardItem(
    card: UiNodeCard,
    onTap: () -> Unit,
    onDrag: (Float, Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .offset(card.x.dp, card.y.dp)
            .pointerInput(card.nodeId) { detectTapGestures { onTap() } }
            .pointerInput(card.nodeId) {
                detectDragGestures { _, dragAmount ->
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
    ) {
        NodeCardView(card = card)
    }
}
