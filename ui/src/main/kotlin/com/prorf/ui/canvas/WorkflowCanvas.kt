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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.prorf.platform.graph.Edge
import com.prorf.ui.model.UiNodeCard

/**
 * L2 UI — Pannable workflow canvas.
 *
 * Coordinate system: node positions are stored as Float values interpreted as dp.
 * Canvas drawing operates in pixels; all position conversions multiply by density.
 * Pan offset is tracked in screen pixels as transient view state (not persisted).
 *
 * Gesture priority: children's detectDragGestures consume pointer events before
 * the parent's background pan gesture sees them, so dragging a node moves the
 * node without panning the canvas (Compose child-first dispatch rule).
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
    val density = LocalDensity.current.density
    // Canvas pan offset in screen pixels; transient UI state, not persisted
    var panOffsetPx by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            // Background single-finger pan; node children consume their own drag first
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    panOffsetPx += dragAmount
                }
            },
    ) {
        EdgeOverlay(
            nodes = nodes,
            edges = edges,
            panOffsetPx = panOffsetPx,
            density = density,
            connectingFromNodeId = connectingFromNodeId,
        )
        nodes.forEach { card ->
            NodeCardItem(
                card = card,
                panOffsetPx = panOffsetPx,
                density = density,
                onTap = { onNodeSelected(card.nodeId) },
                onDrag = { dxPx, dyPx ->
                    // dragAmount from detectDragGestures is in screen pixels;
                    // card positions are stored in dp → divide by density.
                    onNodeMoved(card.nodeId, card.x + dxPx / density, card.y + dyPx / density)
                },
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
    panOffsetPx: Offset,
    density: Float,
    connectingFromNodeId: String? = null,
) {
    val nodeIndex = nodes.associateBy { it.nodeId }
    Canvas(modifier = Modifier.fillMaxSize()) {
        for (edge in edges) {
            val from = nodeIndex[edge.fromNodeId] ?: continue
            val to = nodeIndex[edge.toNodeId] ?: continue
            // Positions are in dp; convert to pixels then apply canvas pan.
            // NodeCardView is 160dp wide; ports are at right-center and left-center.
            val fromXPx = from.x * density + panOffsetPx.x
            val fromYPx = from.y * density + panOffsetPx.y
            val toXPx = to.x * density + panOffsetPx.x
            val toYPx = to.y * density + panOffsetPx.y
            val start = Offset(fromXPx + 160f * density, fromYPx + 24f * density)
            val end = Offset(toXPx, toYPx + 24f * density)
            val cp1 = Offset(start.x + (end.x - start.x) * 0.5f, start.y)
            val cp2 = Offset(end.x - (end.x - start.x) * 0.5f, end.y)
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
                },
                color = Color(0xFF6650A4),
                style = Stroke(width = 2f * density, cap = StrokeCap.Round),
            )
        }
        if (connectingFromNodeId != null) {
            val fromCard = nodeIndex[connectingFromNodeId]
            if (fromCard != null) {
                val fromXPx = fromCard.x * density + panOffsetPx.x
                val fromYPx = fromCard.y * density + panOffsetPx.y
                drawRect(
                    color = Color(0xFF6650A4),
                    topLeft = Offset(fromXPx - 4f * density, fromYPx - 4f * density),
                    size = Size(168f * density, 60f * density),
                    style = Stroke(
                        width = 3f * density,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(10f * density, 5f * density),
                        ),
                    ),
                )
            }
        }
    }
}

@Composable
private fun NodeCardItem(
    card: UiNodeCard,
    panOffsetPx: Offset,
    density: Float,
    onTap: () -> Unit,
    onDrag: (dxPx: Float, dyPx: Float) -> Unit,
) {
    // Pan offset is in pixels; add to dp position after converting to dp.
    val displayX = card.x + panOffsetPx.x / density
    val displayY = card.y + panOffsetPx.y / density
    Box(
        modifier = Modifier
            .offset(displayX.dp, displayY.dp)
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
