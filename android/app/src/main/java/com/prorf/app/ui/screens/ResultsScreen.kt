package com.prorf.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import java.io.File
import java.util.Locale
import com.prorf.app.R
import com.prorf.app.data.*
import kotlinx.serialization.json.Json
import com.prorf.app.ui.components.MetricTile
import com.prorf.app.ui.components.StatusBadge
import com.prorf.app.ui.theme.Prf
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private fun fmtFrequency(mhz: Double): String =
    if (mhz >= 1000) {
        val ghz = mhz / 1000
        if (ghz == ghz.toLong().toDouble()) "${ghz.toLong()} GHz"
        else String.format(Locale.US, "%.2f", ghz).trimEnd('0').trimEnd('.') + " GHz"
    } else "${RfEngine.fmt(mhz)} MHz"

@Composable
fun ResultsScreen(workflow: Workflow, onBack: () -> Unit) {
    val p = Prf.colors
    val context = LocalContext.current
    val result = remember(workflow) { RfEngine.evaluate(workflow.nodes, workflow.globals) }
    var tab by remember { mutableStateOf("summary") }
    val tabs = listOf("summary" to stringResource(R.string.tab_summary), "table" to stringResource(R.string.tab_table), "chart" to stringResource(R.string.tab_chart), "sankey" to stringResource(R.string.tab_sankey))

    Column(Modifier.fillMaxSize().background(p.bg2)) {
        Row(
            Modifier.fillMaxWidth().background(p.surf).padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = p.txt1) }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.link_analysis), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.txt1)
                Text(workflow.name, fontSize = 11.sp, color = p.txt3, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = {
                val text = buildString {
                    appendLine(context.getString(R.string.export_summary_title, workflow.name))
                    appendLine("─".repeat(30))
                    appendLine(context.getString(R.string.export_freq, fmtFrequency(workflow.globals.frequencyMHz)))
                    appendLine(context.getString(R.string.export_nodes, workflow.nodes.size))
                    appendLine()
                    appendLine(context.getString(R.string.export_margin, RfEngine.fmt(result.margin)))
                    appendLine(context.getString(R.string.export_rx_power, RfEngine.fmt(result.rxPwr)))
                    appendLine(context.getString(R.string.export_eirp, RfEngine.fmt(result.eirp)))
                    appendLine(context.getString(R.string.export_snr, RfEngine.fmt(result.snr)))
                    appendLine()
                    appendLine(context.getString(R.string.export_footer))
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_SUBJECT, workflow.name)
                }
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
            }) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share), tint = p.txt1)
            }
            val exportJsonFilename = context.getString(R.string.export_json_filename, workflow.name.replace(" ", "_"))
            IconButton(onClick = {
                runCatching {
                    val json = Json { prettyPrint = true }
                    val jsonStr = json.encodeToString(Workflow.serializer(), workflow)
                    val file = File(context.cacheDir, exportJsonFilename)
                    file.writeText(jsonStr)
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_export_json)))
                }.onFailure {
                    android.util.Log.e("ResultsScreen", "JSON export failed: ${it.message}", it)
                    android.widget.Toast.makeText(context, context.getString(R.string.export_failed), android.widget.Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Default.Download, contentDescription = stringResource(R.string.action_export_json), tint = p.txt1)
            }
        }
        Row(Modifier.fillMaxWidth().background(p.bg)) {
            tabs.forEach { (id, label) ->
                val sel = tab == id
                Column(
                    Modifier.weight(1f).clickable { tab = id }.padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        label, fontSize = 13.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        color = if (sel) p.prim else p.txt3,
                    )
                    Spacer(Modifier.height(9.dp))
                    Box(
                        Modifier.fillMaxWidth().height(2.5.dp)
                            .background(if (sel) p.prim else Color.Transparent),
                    )
                }
            }
        }
        HorizontalDivider(color = p.line2)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            ResultsTabContent(tab, result, workflow.globals)
        }
    }
}

/** Inline results panel for large-screen editor layout. */
@Composable
fun ResultsPanel(result: LinkResult, globals: GlobalParams) {
    var tab by remember { mutableStateOf("summary") }
    val p = Prf.colors
    val tabs = listOf("summary" to stringResource(R.string.tab_summary), "table" to stringResource(R.string.tab_table), "chart" to stringResource(R.string.tab_chart), "sankey" to stringResource(R.string.tab_sankey))
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(p.bg)) {
            tabs.forEach { (id, label) ->
                val sel = tab == id
                Column(
                    Modifier.weight(1f).clickable { tab = id }.padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) p.prim else p.txt3)
                    Spacer(Modifier.height(7.dp))
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (sel) p.prim else Color.Transparent))
                }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp)) {
            ResultsTabContent(tab, result, globals)
        }
    }
}

@Composable
private fun ResultsTabContent(tab: String, result: LinkResult, globals: GlobalParams) {
    when (tab) {
        "summary" -> SummaryTab(result)
        "table" -> TableTab(result)
        "chart" -> ChartCard(result, globals)
        "sankey" -> SankeyCard(result, globals)
    }
}

@Composable
private fun SummaryTab(r: LinkResult) {
    val p = Prf.colors
    if (r.trace.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("📭", fontSize = 44.sp)
            Text(stringResource(R.string.results_empty_title), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = p.txt2)
            Text(stringResource(R.string.results_empty_desc), fontSize = 13.sp, color = p.txt4, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(stringResource(R.string.metric_margin), RfEngine.fmt(r.margin, 2), "dB", if (r.isValid) p.ok else p.err, Modifier.weight(1f), big = true)
            MetricTile(stringResource(R.string.metric_rx_power), RfEngine.fmt(r.rxPwr, 2), "dBm", p.prim, Modifier.weight(1f), big = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(stringResource(R.string.metric_eirp), RfEngine.fmt(r.eirp), "dBm", p.txt2, Modifier.weight(1f))
            MetricTile(stringResource(R.string.metric_snr), RfEngine.fmt(r.snr), "dB", p.sec, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(stringResource(R.string.metric_noise_floor), RfEngine.fmt(r.noiseFloor), "dBm", p.warn, Modifier.weight(1f))
            MetricTile(stringResource(R.string.metric_sensitivity), RfEngine.fmt(r.sensitivity), "dBm", p.txt3, Modifier.weight(1f))
        }
        Column(Modifier.fillMaxWidth().background(p.surf, RoundedCornerShape(14.dp)).padding(16.dp)) {
            Text(stringResource(R.string.link_status), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.txt1)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusBadge(r.isValid, if (r.isValid) stringResource(R.string.link_ok) else stringResource(R.string.link_warn))
                Text(
                    if (r.isValid) stringResource(R.string.margin_ok, RfEngine.fmt(r.margin)) else stringResource(R.string.margin_low, RfEngine.fmt(r.margin)),
                    fontSize = 13.sp, color = p.txt2,
                )
            }
            if (!r.isValid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.link_advice),
                    fontSize = 12.sp, color = p.err,
                    modifier = Modifier.fillMaxWidth().background(p.errTint, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun TableTab(r: LinkResult) {
    val p = Prf.colors
    Column(Modifier.fillMaxWidth().background(p.surf, RoundedCornerShape(14.dp))) {
        Row(Modifier.fillMaxWidth().background(p.bg2, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(stringResource(R.string.col_module), Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.txt3)
            Text(stringResource(R.string.col_gain), Modifier.width(60.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.txt3)
            Text(stringResource(R.string.col_loss), Modifier.width(60.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.txt3)
            Text(stringResource(R.string.col_power), Modifier.width(80.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.txt3)
        }
        r.trace.forEachIndexed { i, t ->
            val meta = p.kind(t.kind)
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).background(meta.col, CircleShape))
                    Text(t.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.txt1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(if (t.gain > 0) "+${RfEngine.fmt(t.gain)}" else "—", Modifier.width(60.dp), fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = p.ok)
                Text(if (t.loss > 0) "−${RfEngine.fmt(t.loss)}" else "—", Modifier.width(60.dp), fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = p.err)
                Text(RfEngine.fmt(t.pwr), Modifier.width(80.dp), fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.prim)
            }
            if (i < r.trace.size - 1) HorizontalDivider(color = p.line2)
        }
    }
}

@Composable
private fun ChartCard(r: LinkResult, globals: GlobalParams) {
    val p = Prf.colors
    val chartTitle = stringResource(R.string.chart_title)
    val noChartData = stringResource(R.string.no_chart_data)
    val chartXLabel = stringResource(R.string.chart_x_label)
    val legendSnr = stringResource(R.string.legend_snr)
    val legendMargin = stringResource(R.string.legend_margin)
    val legendDist = stringResource(R.string.legend_distance)
    Column(Modifier.fillMaxWidth().background(p.surf, RoundedCornerShape(14.dp)).padding(16.dp)) {
        Text(chartTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.txt1)
        Spacer(Modifier.height(12.dp))
        val data = r.chart
        if (data.isEmpty()) {
            Text(noChartData, color = p.txt3, modifier = Modifier.padding(40.dp))
            return@Column
        }
        val txtColor = android.graphics.Color.argb(
            255, (p.txt4.red * 255).toInt(), (p.txt4.green * 255).toInt(), (p.txt4.blue * 255).toInt(),
        )
        Canvas(Modifier.fillMaxWidth().aspectRatio(1.6f)) {
            val plLeft = 110f; val plRight = 30f; val plTop = 20f; val plBottom = 60f
            val cw = size.width - plLeft - plRight
            val ch = size.height - plTop - plBottom
            val snrs = data.map { it.snr }; val margins = data.map { it.margin }
            val yMin = min(min(snrs.min(), margins.min()), -20.0)
            val yMax = max(max(snrs.max(), margins.max()), 40.0)
            val xMin = log10(max(data.first().distKm, 0.001)); val xMax = log10(max(data.last().distKm, 0.001))
            val xRange = max(xMax - xMin, 0.001)
            fun tx(d: Double) = plLeft + ((log10(max(d, 0.001)) - xMin) / xRange * cw).toFloat()
            val yRange = max(yMax - yMin, 0.001)
            fun ty(v: Double) = plTop + ((1 - (v - yMin) / yRange) * ch).toFloat()

            // grid + labels — CX21: dynamic grid intervals
            val paint = android.graphics.Paint().apply { color = txtColor; textSize = 26f; textAlign = android.graphics.Paint.Align.RIGHT }
            val yStep = when {
                yRange <= 20 -> 5.0
                yRange <= 60 -> 10.0
                yRange <= 150 -> 20.0
                else -> 50.0
            }
            val yStart = kotlin.math.floor(yMin / yStep) * yStep
            generateSequence(yStart) { it + yStep }.takeWhile { it <= yMax + yStep * 0.01 }.forEach { v ->
                drawLine(p.line2, Offset(plLeft, ty(v)), Offset(size.width - plRight, ty(v)), 2f)
                drawContext.canvas.nativeCanvas.drawText(String.format(Locale.US, "%.1f", v), plLeft - 10f, ty(v) + 9f, paint)
            }
            // zero dashed
            if (yMin < 0 && yMax > 0) drawLine(
                p.err.copy(alpha = .5f), Offset(plLeft, ty(0.0)), Offset(size.width - plRight, ty(0.0)),
                2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
            )
            // curves
            fun path(values: List<Double>): Path {
                val path = Path()
                values.forEachIndexed { i, v ->
                    val x = tx(data[i].distKm); val y = ty(v)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                return path
            }
            drawPath(path(snrs), p.prim, style = Stroke(4f))
            drawPath(path(margins), p.ok, style = Stroke(4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))))
            // current distance marker
            val dx = tx(globals.distanceKm.coerceIn(data.first().distKm, data.last().distKm))
            drawLine(p.warn, Offset(dx, plTop), Offset(dx, size.height - plBottom), 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
            // axes
            drawLine(p.line, Offset(plLeft, plTop), Offset(plLeft, size.height - plBottom), 3f)
            drawLine(p.line, Offset(plLeft, size.height - plBottom), Offset(size.width - plRight, size.height - plBottom), 3f)
            // V1: X-axis numeric tick labels (log-scale distance values)
            val xTickPaint = android.graphics.Paint().apply { color = txtColor; textSize = 22f; textAlign = android.graphics.Paint.Align.CENTER }
            val tickPowers = listOf(-2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
            for (pw in tickPowers) {
                val distVal = 10.0.pow(pw)
                if (distVal >= data.first().distKm * 0.5 && distVal <= data.last().distKm * 1.5) {
                    val tx2 = plLeft + ((pw - xMin) / xRange * cw).toFloat()
                    if (tx2 >= plLeft - 5f && tx2 <= size.width - plRight + 5f) {
                        drawLine(p.line2, Offset(tx2, size.height - plBottom), Offset(tx2, size.height - plBottom + 8f), 2f)
                        val lbl = if (distVal < 1.0) "${(distVal * 1000).toInt()}m"
                            else if (distVal == distVal.toLong().toDouble()) "${distVal.toLong()}"
                            else String.format(Locale.US, "%.1f", distVal)
                        drawContext.canvas.nativeCanvas.drawText(lbl, tx2, size.height - plBottom + 24f, xTickPaint)
                    }
                }
            }
            val axisPaint = android.graphics.Paint().apply { color = txtColor; textSize = 26f; textAlign = android.graphics.Paint.Align.CENTER }
            drawContext.canvas.nativeCanvas.drawText(chartXLabel, plLeft + cw / 2, size.height - 10f, axisPaint)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(p.prim, legendSnr)
            LegendItem(p.ok, legendMargin)
            LegendItem(p.warn, legendDist)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    val p = Prf.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.width(18.dp).height(3.dp).background(color))
        Text(label, fontSize = 11.sp, color = p.txt3)
    }
}

@Composable
private fun SankeyCard(r: LinkResult, globals: GlobalParams) {
    val p = Prf.colors
    val sankeyTitle = stringResource(R.string.sankey_title)
    val noData = stringResource(R.string.no_data)
    val lgGain = stringResource(R.string.legend_gain)
    val lgLoss = stringResource(R.string.legend_loss)
    val lgPower = stringResource(R.string.legend_power)
    val contextLine = stringResource(
        R.string.sankey_context,
        fmtFrequency(globals.frequencyMHz),
        RfEngine.fmt(globals.distanceKm),
        RfEngine.fmt(globals.bandwidthMHz),
    )
    Column(Modifier.fillMaxWidth().background(p.surf, RoundedCornerShape(14.dp)).padding(16.dp)) {
        Text(sankeyTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.txt1)
        Text(contextLine, fontSize = 11.sp, color = p.txt4)
        Spacer(Modifier.height(12.dp))
        if (r.trace.isEmpty()) {
            Text(noData, color = p.txt3, modifier = Modifier.padding(40.dp))
            return@Column
        }
        val labelColor = android.graphics.Color.argb(
            255, (p.txt3.red * 255).toInt(), (p.txt3.green * 255).toInt(), (p.txt3.blue * 255).toInt(),
        )
        Canvas(Modifier.fillMaxWidth().aspectRatio(2.2f)) {
            val n = r.trace.size
            val gap = 24f
            val left = 40f
            val nodeW = max(32f, (size.width - left - 16f - gap * (n - 1)) / n * 0.7f)
            val cy = size.height / 2
            val maxP = max(r.trace.maxOf { kotlin.math.abs(it.pwr) }, 10.0)
            fun barH(v: Double) = max(6f, (kotlin.math.abs(v) / maxP * size.height * 0.32).toFloat())
            val paint = android.graphics.Paint().apply { color = labelColor; textSize = 24f; textAlign = android.graphics.Paint.Align.CENTER }
            r.trace.forEachIndexed { i, t ->
                val meta = p.kind(t.kind)
                val x = left + i * (nodeW + gap)
                val ph = barH(t.pwr)
                if (i > 0) drawRect(meta.col.copy(alpha = .25f), Offset(x - gap, cy - ph / 2 - 2), androidx.compose.ui.geometry.Size(gap, ph + 4))
                drawRoundRect(meta.col.copy(alpha = .8f), Offset(x, cy - ph / 2), androidx.compose.ui.geometry.Size(nodeW, ph), androidx.compose.ui.geometry.CornerRadius(8f))
                if (t.gain > 0) {
                    val gh = barH(t.gain).coerceAtMost(cy - ph / 2 - 40f)
                    drawRoundRect(p.ok.copy(alpha = .6f), Offset(x, cy - ph / 2 - gh - 4), androidx.compose.ui.geometry.Size(nodeW, gh), androidx.compose.ui.geometry.CornerRadius(6f))
                }
                if (t.loss > 0) {
                    val lh = barH(t.loss).coerceAtMost(size.height - cy - ph / 2 - 44f)
                    drawRoundRect(p.err.copy(alpha = .4f), Offset(x, cy + ph / 2 + 4), androidx.compose.ui.geometry.Size(nodeW, lh), androidx.compose.ui.geometry.CornerRadius(6f))
                }
                drawContext.canvas.nativeCanvas.drawText(t.pwr.toInt().toString(), x + nodeW / 2, cy - ph / 2 - (if (t.gain > 0) barH(t.gain) + 14f else 10f), paint)
                drawContext.canvas.nativeCanvas.drawText(t.name.take(8), x + nodeW / 2, size.height - 8f, paint)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendSwatch(p.ok, lgGain); LegendSwatch(p.err, lgLoss); LegendSwatch(p.prim, lgPower)
        }
    }
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    val p = Prf.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(10.dp).background(color.copy(alpha = .8f), RoundedCornerShape(2.dp)))
        Text(label, fontSize = 11.sp, color = p.txt3)
    }
}
