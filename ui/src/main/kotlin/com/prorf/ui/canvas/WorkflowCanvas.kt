package com.prorf.ui.canvas

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.prorf.engineering.quantity.Quantity
import com.prorf.platform.graph.Edge
import com.prorf.ui.model.UiNodeCard
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * L2 UI — Pannable/draggable workflow canvas.
 *
 * Coordinate system: node positions are stored in dp. Canvas drawing operates in pixels;
 * all position values multiply by density. Pan offset is screen-pixel transient view state.
 *
 * Gesture dispatch: children's detectDragGestures win over the parent pan gesture
 * (Compose child-first dispatch), so dragging a node moves the node, not the canvas.
 */
@Composable
fun WorkflowCanvas(
    nodes: List<UiNodeCard>,
    edges: List<Edge>,
    onNodeSelected: (nodeId: String) -> Unit,
    onNodeMoved: (nodeId: String, x: Float, y: Float) -> Unit,
    connectingFromNodeId: String? = null,
    executionOutputs: Map<String, Map<String, Any>> = emptyMap(),
    scale: Float = 1f,
    onScaleChanged: (Float) -> Unit = {},
    resetViewKey: Int = 0,
    modifier: Modifier = Modifier,
) {
    val localDensity = LocalDensity.current
    val density = localDensity.density
    val fontScale = localDensity.fontScale
    var panOffsetPx by remember { mutableStateOf(Offset.Zero) }
    val latestScale = rememberUpdatedState(scale)
    val latestOnScaleChanged = rememberUpdatedState(onScaleChanged)
    LaunchedEffect(resetViewKey) { if (resetViewKey > 0) panOffsetPx = Offset.Zero }

    val gridDotColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    // Edge label rendering — captured from composable scope, reused inside DrawScope
    val edgeLabelBgArgb = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f).toArgb()
    val edgeLabelTextSizePx = 9f * density * fontScale * scale.coerceIn(0.4f, 2.5f)
    val labelBgPaint = remember {
        android.graphics.Paint().apply { isAntiAlias = true }
    }
    val labelTextPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
    }
    val labelTextBounds = remember { android.graphics.Rect() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    panOffsetPx += pan
                    val current = latestScale.value
                    val newScale = (current * zoom).coerceIn(0.4f, 2.5f)
                    if (newScale != current) latestOnScaleChanged.value(newScale)
                }
            },
    ) {
        // Layer 1: dot grid background
        Canvas(Modifier.fillMaxSize()) {
            val step = (24.dp * scale.coerceIn(0.4f, 2.5f)).toPx()
            val r = (1.dp * scale.coerceIn(0.6f, 1.4f)).toPx()
            var x = panOffsetPx.x % step
            if (x < 0f) x += step
            while (x < size.width) {
                var y = panOffsetPx.y % step
                if (y < 0f) y += step
                while (y < size.height) {
                    drawCircle(gridDotColor, r, Offset(x, y))
                    y += step
                }
                x += step
            }
        }

        // Layer 2: edges + value labels
        Canvas(Modifier.fillMaxSize()) {
            val nodeIndex = nodes.associateBy { it.nodeId }
            val s = scale.coerceIn(0.4f, 2.5f)
            for (edge in edges) {
                val from = nodeIndex[edge.fromNodeId] ?: continue
                val to = nodeIndex[edge.toNodeId] ?: continue

                val fxPx = from.x * density * s + panOffsetPx.x
                val fyPx = from.y * density * s + panOffsetPx.y
                val txPx = to.x * density * s + panOffsetPx.x
                val tyPx = to.y * density * s + panOffsetPx.y

                // Source node's category color for the edge line
                val lineColor = edgeNodeColor(from.typeId).copy(alpha = 0.72f)

                // Output port (right edge center) → input port (left edge center)
                val start = Offset(fxPx + NODE_CARD_WIDTH_DP * density * s, fyPx + NODE_PORT_Y_DP * density * s)
                val end = Offset(txPx, tyPx + NODE_PORT_Y_DP * density * s)
                val dx = end.x - start.x
                val cp1 = Offset(start.x + dx * 0.45f, start.y)
                val cp2 = Offset(end.x - dx * 0.45f, end.y)

                drawPath(
                    path = Path().apply {
                        moveTo(start.x, start.y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
                    },
                    color = lineColor,
                    style = Stroke(width = 2f * density * s, cap = StrokeCap.Round),
                )

                // Arrowhead at target end — destination category color
                val arrowColor = edgeNodeColor(to.typeId).copy(alpha = 0.72f)
                val angle = atan2((end.y - cp2.y).toDouble(), (end.x - cp2.x).toDouble())
                val arrowLen = 8f * density * s
                val spread = Math.PI / 6.0
                drawPath(
                    path = Path().apply {
                        moveTo(
                            (end.x - arrowLen * cos(angle - spread)).toFloat(),
                            (end.y - arrowLen * sin(angle - spread)).toFloat(),
                        )
                        lineTo(end.x, end.y)
                        lineTo(
                            (end.x - arrowLen * cos(angle + spread)).toFloat(),
                            (end.y - arrowLen * sin(angle + spread)).toFloat(),
                        )
                    },
                    color = arrowColor,
                    style = Stroke(width = 2f * density * s, cap = StrokeCap.Round),
                )

                // Edge value label — drawn at bezier midpoint when source node has output
                val edgeDist = sqrt((end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y))
                val primaryOutput = if (edgeDist > 48.dp.toPx()) {
                    executionOutputs[edge.fromNodeId]?.values?.firstOrNull()
                } else null

                if (primaryOutput != null) {
                    val labelText = formatEdgeValue(primaryOutput)
                    // Bezier midpoint at t=0.5 for symmetric control points equals (start+end)/2
                    val midX = (start.x + end.x) / 2f
                    val midY = (start.y + end.y) / 2f

                    labelTextPaint.color = lineColor.copy(alpha = 1f).toArgb()
                    labelTextPaint.textSize = edgeLabelTextSizePx
                    labelTextPaint.getTextBounds(labelText, 0, labelText.length, labelTextBounds)

                    val textW = labelTextPaint.measureText(labelText)
                    val pH = 5.dp.toPx()
                    val pV = 2.5f.dp.toPx()
                    val cornerR = 4.dp.toPx()

                    labelBgPaint.color = edgeLabelBgArgb

                    drawContext.canvas.nativeCanvas.apply {
                        drawRoundRect(
                            midX - textW / 2f - pH,
                            midY + labelTextBounds.top - pV,
                            midX + textW / 2f + pH,
                            midY + labelTextBounds.bottom + pV,
                            cornerR, cornerR,
                            labelBgPaint,
                        )
                        drawText(labelText, midX, midY, labelTextPaint)
                    }
                }
            }

            // Connecting-from source: dashed highlight rect
            if (connectingFromNodeId != null) {
                val fromCard = nodeIndex[connectingFromNodeId]
                if (fromCard != null) {
                    val fx = fromCard.x * density * s + panOffsetPx.x
                    val fy = fromCard.y * density * s + panOffsetPx.y
                    drawRect(
                        color = edgeNodeColor(fromCard.typeId),
                        topLeft = Offset(fx - 4f * density * s, fy - 4f * density * s),
                        size = Size((NODE_CARD_WIDTH_DP + 8f) * density * s, (NODE_PORT_Y_DP * 2f + 8f) * density * s),
                        style = Stroke(
                            width = 2f * density * s,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * density * s, 4f * density * s)),
                        ),
                    )
                }
            }
        }

        // Layer 2.5: selection glow — drawn above edges, below node cards
        val selectionGlowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        val scaleClamped = scale.coerceIn(0.4f, 2.5f)
        Canvas(Modifier.fillMaxSize()) {
            nodes.filter { it.isSelected }.forEach { card ->
                val sx = card.x * density * scaleClamped + panOffsetPx.x
                val sy = card.y * density * scaleClamped + panOffsetPx.y
                val pad = 8f * density * scaleClamped
                drawRoundRect(
                    color = selectionGlowColor,
                    topLeft = Offset(sx - pad, sy - pad),
                    size = Size(NODE_CARD_WIDTH_DP * density * scaleClamped + pad * 2f, NODE_PORT_Y_DP * 2f * density * scaleClamped + pad * 2f),
                    cornerRadius = CornerRadius((12.dp * scaleClamped).toPx()),
                )
            }
        }

        // Layer 3: node cards — positions and visual size both scale with zoom
        val sc = scale.coerceIn(0.4f, 2.5f)
        nodes.forEach { card ->
            val displayX = card.x * sc + panOffsetPx.x / density
            val displayY = card.y * sc + panOffsetPx.y / density
            Box(
                modifier = Modifier
                    .offset(displayX.dp, displayY.dp)
                    .pointerInput(card.nodeId) { detectTapGestures { onNodeSelected(card.nodeId) } }
                    .pointerInput(card.nodeId) {
                        detectDragGestures { _, dragAmount ->
                            onNodeMoved(
                                card.nodeId,
                                snapToGrid(card.x + dragAmount.x / density / sc),
                                snapToGrid(card.y + dragAmount.y / density / sc),
                            )
                        }
                    },
            ) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = sc
                        scaleY = sc
                        transformOrigin = TransformOrigin(0f, 0f)
                    },
                ) {
                    NodeCardView(card = card)
                }
            }
        }

        // Layer 4: connect mode banner
        if (connectingFromNodeId != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryContainer.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.TopCenter),
            ) {
                Text(
                    text = "Tap a target node to connect — tap Cancel to abort",
                    style = MaterialTheme.typography.labelMedium,
                    color = onPrimaryContainer,
                )
            }
        }
    }
}

/** Snaps a dp coordinate to the nearest 24dp grid point. */
private fun snapToGrid(value: Float, step: Float = 24f): Float =
    (Math.round(value / step).toFloat()) * step

/** Formats a node output value compactly for edge labels. */
private fun formatEdgeValue(value: Any): String = when (value) {
    is Quantity -> "%.1f %s".format(value.value, value.unit.symbol)
    is Double -> "%.2f".format(value)
    is Float -> "%.2f".format(value)
    else -> value.toString()
}

/** Maps typeId to a category accent color — mirrors NodeCardView's categoryColor without coupling. */
private fun edgeNodeColor(typeId: String): Color {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Source") || name.contains("Signal") || name.contains("Noise") -> Color(0xFF2F80ED)
        name.contains("Amplifier") -> Color(0xFF27AE60)
        name.contains("Attenuator") || name.contains("Cable") || name.contains("Filter") -> Color(0xFFF2994A)
        name.contains("Loss") || name.contains("Channel") || name.contains("Path") -> Color(0xFF9B51E0)
        name.contains("Receiver") || name.contains("Sensitivity") -> Color(0xFFEB5757)
        else -> Color(0xFF2F80ED)
    }
}
