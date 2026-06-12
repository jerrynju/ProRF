package com.prorf.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.prorf.app.ui.theme.NumericFont
import androidx.compose.ui.res.stringResource
import com.prorf.app.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.app.data.RfEngine
import com.prorf.app.ui.theme.Prf
import kotlin.math.roundToInt

@Composable
fun StatusBadge(ok: Boolean, label: String) {
    val p = Prf.colors
    val col = if (ok) p.ok else p.err
    val bg = if (ok) p.okTint else p.errTint
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Box(Modifier.size(7.dp).background(col, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = col)
    }
}

@Composable
fun SectionLabel(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    val p = Prf.colors
    Row(
        Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = p.txt1)
        if (action != null) {
            Text(
                action, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.prim,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onAction?.invoke() },
            )
        }
    }
}

@Composable
fun MetricTile(label: String, value: String, unit: String, accent: Color, modifier: Modifier = Modifier, big: Boolean = false) {
    val p = Prf.colors
    Column(
        modifier
            .background(p.surf, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label.uppercase(), fontSize = 10.sp, color = p.txt4, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value, fontSize = if (big) 26.sp else 19.sp, fontWeight = FontWeight.ExtraBold,
                color = accent, fontFamily = NumericFont,
            )
            Spacer(Modifier.width(4.dp))
            Text(unit, fontSize = 11.sp, color = p.txt4, modifier = Modifier.padding(bottom = 3.dp))
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, unit: String, accent: Color, modifier: Modifier = Modifier) {
    val p = Prf.colors
    Column(
        modifier
            .background(p.bg2, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Text(label, fontSize = 9.sp, color = p.txt4, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = accent, fontFamily = NumericFont, maxLines = 1)
        Text(unit, fontSize = 9.sp, color = p.txt4)
    }
}

@Composable
fun Chips(items: List<Pair<String, String>>, active: String, onSelect: (String) -> Unit) {
    val p = Prf.colors
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { (id, label) ->
            val sel = id == active
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                color = if (sel) p.onPrim else p.txt2,
                modifier = Modifier
                    .background(if (sel) p.prim else p.surf, RoundedCornerShape(999.dp))
                    .border(1.dp, if (sel) p.prim else p.line2, RoundedCornerShape(999.dp))
                    .clickable { onSelect(id) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }
}

/** Parameter row: label + numeric input + slider, matching the design's PRow. */
@Composable
fun ParamRow(label: String, value: Double, unit: String, min: Double, max: Double, onChange: (Double) -> Unit) {
    val p = Prf.colors
    var text by remember(value) { mutableStateOf(trimNum(value)) }
    // CX18: clamp text when focus is lost
    fun clampText() {
        val clamped = (text.toDoubleOrNull() ?: value).coerceIn(min, max)
        text = trimNum(clamped)
        onChange(clamped)
    }
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = p.txt1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicNumberField(text, onText = { t ->
                    text = t
                    t.toDoubleOrNull()?.let { onChange(it.coerceIn(min, max)) }
                }, onFocusLost = { clampText() })
                Spacer(Modifier.width(4.dp))
                Text(unit, fontSize = 12.sp, color = p.txt3)
            }
        }
        Slider(
            value = value.toFloat().coerceIn(min.toFloat(), max.toFloat()),
            onValueChange = { onChange(quantize(it.toDouble(), min, max)) },
            valueRange = min.toFloat()..max.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = p.prim, activeTrackColor = p.prim, inactiveTrackColor = p.surf3,
            ),
        )
    }
}

@Composable
private fun BasicNumberField(text: String, onText: (String) -> Unit, onFocusLost: () -> Unit = {}) {
    val p = Prf.colors
    var hasFocus by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = text,
        onValueChange = onText,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = NumericFont, color = p.prim,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.width(80.dp).height(40.dp)
            .onFocusChanged { state ->
                if (hasFocus && !state.isFocused) onFocusLost()
                hasFocus = state.isFocused
            },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = p.prim, unfocusedBorderColor = p.line,
            focusedContainerColor = p.surf, unfocusedContainerColor = p.surf,
        ),
    )
}

private fun quantize(v: Double, min: Double, max: Double): Double {
    val range = max - min
    val step = when {
        range <= 1.0 -> 0.001
        range <= 10.0 -> 0.01
        range <= 100.0 -> 0.1
        range <= 1000.0 -> 1.0
        range <= 10000.0 -> 10.0
        range <= 100000.0 -> 100.0
        else -> 1000.0
    }
    return (kotlin.math.round(v / step) * step).coerceIn(min, max)
}

fun trimNum(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else RfEngine.fmt(v, 1)

/** Format frequency for compact display. Returns value string and unit string separately. */
fun fmtFreqShort(mhz: Double): Pair<String, String> =
    if (mhz >= 1000.0) {
        val ghz = mhz / 1000.0
        val s = if (ghz == ghz.toLong().toDouble()) ghz.toLong().toString()
                else "%.3f".format(java.util.Locale.US, ghz).trimEnd('0').trimEnd('.')
        s to "GHz"
    } else {
        RfEngine.fmt(mhz) to "MHz"
    }

/** V3: Format a "yyyy-MM-dd HH:mm" timestamp as relative time (e.g. "5 min ago"). */
@Composable
fun relativeTime(dateStr: String): String {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return remember(dateStr) {
        runCatching {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", java.util.Locale.US)
            val then = java.time.LocalDateTime.parse(dateStr, formatter)
            val now = java.time.LocalDateTime.now()
            val minutes = java.time.Duration.between(then, now).toMinutes()
            when {
                minutes < 1 -> ctx.getString(com.prorf.app.R.string.time_just_now)
                minutes < 60 -> ctx.getString(com.prorf.app.R.string.time_minutes_ago, minutes)
                minutes < 1440 -> ctx.getString(com.prorf.app.R.string.time_hours_ago, minutes / 60)
                minutes < 43200 -> ctx.getString(com.prorf.app.R.string.time_days_ago, minutes / 1440)
                else -> dateStr
            }
        }.getOrDefault(dateStr)
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, tonal: Boolean = false, onClick: () -> Unit) {
    val p = Prf.colors
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = if (tonal) ButtonDefaults.buttonColors(containerColor = p.surf2, contentColor = p.txt1)
        else ButtonDefaults.buttonColors(containerColor = p.prim, contentColor = p.onPrim),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// ── Template i18n helpers (shared by HomeScreen & TemplatesScreen) ──

@Composable
fun templateName(tplId: String): String = stringResource(
    when (tplId) {
        "sat" -> R.string.tpl_sat
        "5g" -> R.string.tpl_5g
        "mw" -> R.string.tpl_mw
        "radar" -> R.string.tpl_radar_name
        else -> R.string.tpl_blank
    },
)

@Composable
fun templateDesc(tplId: String): String = stringResource(
    when (tplId) {
        "sat" -> R.string.tpl_sat_desc
        "5g" -> R.string.tpl_5g_desc
        "mw" -> R.string.tpl_mw_desc
        "radar" -> R.string.tpl_radar_desc
        else -> R.string.tpl_blank_desc
    },
)
