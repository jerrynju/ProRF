package com.prorf.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prorf.app.data.NodeKind
import com.prorf.app.ui.theme.Prf
import kotlin.math.cos
import kotlin.math.sin

/**
 * Uniform vector icon for each RF module.
 * [iconKey] matches the `emoji` field in ModuleSpec (repurposed as icon key).
 */
@Composable
fun ModuleIcon(
    iconKey: String,
    kind: NodeKind,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    val col = Prf.colors.kind(kind).col
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width
        val sw = (s * 0.095f).coerceAtLeast(1.5f)
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val thinStroke = Stroke(width = sw * 0.75f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (iconKey) {
            "sig"  -> drawSig(col, s, stroke)
            "amp"  -> drawAmp(col, s, stroke)
            "ant"  -> drawAntTx(col, s, stroke)
            "anr"  -> drawAntRx(col, s, stroke)
            "cab"  -> drawCab(col, s, stroke, thinStroke)
            "flt"  -> drawFlt(col, s, stroke, thinStroke)
            "att"  -> drawAtt(col, s, stroke)
            "spc"  -> drawSpc(col, s, stroke)
            "rain" -> drawRain(col, s, stroke)
            "atm"  -> drawAtm(col, s, stroke)
            "lna"  -> drawLna(col, s, stroke, thinStroke)
            "mix"  -> drawMix(col, s, stroke)
            "rcv"  -> drawRcv(col, s, stroke, thinStroke)
            else   -> drawCircle(col, s * 0.36f, Offset(s / 2f, s / 2f), style = stroke)
        }
    }
}

// ── Signal Source: sine wave ──────────────────────────────────
private fun DrawScope.drawSig(col: Color, s: Float, stroke: Stroke) {
    val path = Path().apply {
        moveTo(s * 0.05f, s * 0.5f)
        cubicTo(s * 0.18f, s * 0.08f, s * 0.32f, s * 0.08f, s * 0.5f, s * 0.5f)
        cubicTo(s * 0.68f, s * 0.92f, s * 0.82f, s * 0.92f, s * 0.95f, s * 0.5f)
    }
    drawPath(path, col, style = stroke)
}

// ── Power Amplifier: right-pointing triangle ─────────────────
private fun DrawScope.drawAmp(col: Color, s: Float, stroke: Stroke) {
    val path = Path().apply {
        moveTo(s * 0.15f, s * 0.12f)
        lineTo(s * 0.85f, s * 0.5f)
        lineTo(s * 0.15f, s * 0.88f)
        close()
    }
    drawPath(path, col.copy(alpha = 0.16f))
    drawPath(path, col, style = stroke)
}

// ── TX Antenna: upward V + mast + ground ─────────────────────
private fun DrawScope.drawAntTx(col: Color, s: Float, stroke: Stroke) {
    val sw = stroke.width
    // Mast
    drawLine(col, Offset(s * 0.5f, s * 0.74f), Offset(s * 0.5f, s * 0.42f), sw, StrokeCap.Round)
    // Arms
    drawLine(col, Offset(s * 0.5f, s * 0.42f), Offset(s * 0.14f, s * 0.60f), sw, StrokeCap.Round)
    drawLine(col, Offset(s * 0.5f, s * 0.42f), Offset(s * 0.86f, s * 0.60f), sw, StrokeCap.Round)
    // Ground bar
    drawLine(col, Offset(s * 0.22f, s * 0.77f), Offset(s * 0.78f, s * 0.77f), sw, StrokeCap.Round)
    // Ground ticks
    for (i in 0..2) {
        val x = s * (0.31f + i * 0.19f)
        drawLine(col, Offset(x, s * 0.77f), Offset(x - s * 0.06f, s * 0.88f), sw * 0.8f, StrokeCap.Round)
    }
}

// ── RX Antenna: downward V + mast + ground ───────────────────
private fun DrawScope.drawAntRx(col: Color, s: Float, stroke: Stroke) {
    val sw = stroke.width
    // Mast
    drawLine(col, Offset(s * 0.5f, s * 0.26f), Offset(s * 0.5f, s * 0.58f), sw, StrokeCap.Round)
    // Arms
    drawLine(col, Offset(s * 0.5f, s * 0.58f), Offset(s * 0.14f, s * 0.40f), sw, StrokeCap.Round)
    drawLine(col, Offset(s * 0.5f, s * 0.58f), Offset(s * 0.86f, s * 0.40f), sw, StrokeCap.Round)
    // Ground bar
    drawLine(col, Offset(s * 0.22f, s * 0.23f), Offset(s * 0.78f, s * 0.23f), sw, StrokeCap.Round)
    // Ground ticks
    for (i in 0..2) {
        val x = s * (0.31f + i * 0.19f)
        drawLine(col, Offset(x, s * 0.23f), Offset(x + s * 0.06f, s * 0.12f), sw * 0.8f, StrokeCap.Round)
    }
}

// ── Cable / Waveguide: connector–line–connector ──────────────
private fun DrawScope.drawCab(col: Color, s: Float, stroke: Stroke, thin: Stroke) {
    val cw = s * 0.17f; val ch = s * 0.38f; val top = s * 0.31f
    drawRect(col, topLeft = Offset(s * 0.04f, top), size = Size(cw, ch), style = stroke)
    drawRect(col, topLeft = Offset(s * 0.79f, top), size = Size(cw, ch), style = stroke)
    drawLine(col, Offset(s * 0.21f, s * 0.5f), Offset(s * 0.79f, s * 0.5f), thin.width, StrokeCap.Round)
    // Three shield dots
    for (i in 0..2) {
        drawCircle(col, thin.width * 0.55f, Offset(s * (0.33f + i * 0.17f), s * 0.5f))
    }
}

// ── Filter: box with 3 vertical lines ────────────────────────
private fun DrawScope.drawFlt(col: Color, s: Float, stroke: Stroke, thin: Stroke) {
    drawRect(col, topLeft = Offset(s * 0.09f, s * 0.20f), size = Size(s * 0.82f, s * 0.60f), style = stroke)
    for (i in 0..2) {
        val x = s * (0.27f + i * 0.23f)
        drawLine(col, Offset(x, s * 0.28f), Offset(x, s * 0.72f), thin.width, StrokeCap.Round)
    }
}

// ── Attenuator: box with × ────────────────────────────────────
private fun DrawScope.drawAtt(col: Color, s: Float, stroke: Stroke) {
    drawRect(col, topLeft = Offset(s * 0.09f, s * 0.20f), size = Size(s * 0.82f, s * 0.60f), style = stroke)
    drawLine(col, Offset(s * 0.21f, s * 0.28f), Offset(s * 0.79f, s * 0.72f), stroke.width, StrokeCap.Round)
    drawLine(col, Offset(s * 0.79f, s * 0.28f), Offset(s * 0.21f, s * 0.72f), stroke.width, StrokeCap.Round)
}

// ── Free Space: expanding arcs ────────────────────────────────
private fun DrawScope.drawSpc(col: Color, s: Float, stroke: Stroke) {
    val cx = s * 0.5f; val cy = s * 0.72f
    for (i in 0..2) {
        val r = s * (0.18f + i * 0.18f)
        drawArc(col, 195f, 150f, false, Offset(cx - r, cy - r), Size(r * 2f, r * 2f), style = stroke)
    }
    drawCircle(col, stroke.width * 0.7f, Offset(cx, cy))
}

// ── Rain: paired angled drops ─────────────────────────────────
private fun DrawScope.drawRain(col: Color, s: Float, stroke: Stroke) {
    val sw = stroke.width
    listOf(0.26f, 0.5f, 0.74f).forEach { xf ->
        val ox = s * xf
        drawLine(col, Offset(ox, s * 0.13f), Offset(ox - s * 0.07f, s * 0.40f), sw, StrokeCap.Round)
        drawLine(col, Offset(ox, s * 0.54f), Offset(ox - s * 0.07f, s * 0.81f), sw, StrokeCap.Round)
    }
}

// ── Atmospheric: three curved arcs ───────────────────────────
private fun DrawScope.drawAtm(col: Color, s: Float, stroke: Stroke) {
    listOf(0.26f, 0.50f, 0.74f).forEach { yf ->
        val path = Path().apply {
            moveTo(s * 0.10f, s * yf)
            cubicTo(s * 0.28f, s * (yf - 0.12f), s * 0.72f, s * (yf - 0.12f), s * 0.90f, s * yf)
        }
        drawPath(path, col, style = stroke)
    }
}

// ── LNA: compact triangle + asterisk (low-noise indicator) ───
private fun DrawScope.drawLna(col: Color, s: Float, stroke: Stroke, thin: Stroke) {
    val path = Path().apply {
        moveTo(s * 0.10f, s * 0.12f)
        lineTo(s * 0.70f, s * 0.5f)
        lineTo(s * 0.10f, s * 0.88f)
        close()
    }
    drawPath(path, col.copy(alpha = 0.16f))
    drawPath(path, col, style = stroke)
    // Asterisk "*" (3-spoke)
    val ax = s * 0.86f; val ay = s * 0.5f; val ar = s * 0.11f
    for (i in 0..2) {
        val rad = (i * Math.PI / 3.0).toFloat()
        drawLine(
            col,
            Offset(ax + ar * cos(rad), ay + ar * sin(rad)),
            Offset(ax - ar * cos(rad), ay - ar * sin(rad)),
            thin.width, StrokeCap.Round,
        )
    }
}

// ── Mixer: circle with × ─────────────────────────────────────
private fun DrawScope.drawMix(col: Color, s: Float, stroke: Stroke) {
    drawCircle(col, s * 0.42f, Offset(s / 2f, s / 2f), style = stroke)
    val r = s * 0.27f; val cx = s / 2f; val cy = s / 2f
    drawLine(col, Offset(cx - r, cy - r), Offset(cx + r, cy + r), stroke.width, StrokeCap.Round)
    drawLine(col, Offset(cx + r, cy - r), Offset(cx - r, cy + r), stroke.width, StrokeCap.Round)
}

// ── Receiver: rectangle with V antenna ───────────────────────
private fun DrawScope.drawRcv(col: Color, s: Float, stroke: Stroke, thin: Stroke) {
    val sw = stroke.width
    drawRect(col, topLeft = Offset(s * 0.09f, s * 0.45f), size = Size(s * 0.82f, s * 0.44f), style = stroke)
    // V antenna
    drawLine(col, Offset(s * 0.5f, s * 0.45f), Offset(s * 0.27f, s * 0.15f), sw, StrokeCap.Round)
    drawLine(col, Offset(s * 0.5f, s * 0.45f), Offset(s * 0.73f, s * 0.15f), sw, StrokeCap.Round)
    // Display lines inside box
    drawLine(col, Offset(s * 0.20f, s * 0.60f), Offset(s * 0.80f, s * 0.60f), thin.width, StrokeCap.Round)
    drawLine(col, Offset(s * 0.20f, s * 0.73f), Offset(s * 0.62f, s * 0.73f), thin.width, StrokeCap.Round)
}
