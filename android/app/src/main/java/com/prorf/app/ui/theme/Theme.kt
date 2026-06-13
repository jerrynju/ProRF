package com.prorf.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.annotation.StringRes
import com.prorf.app.data.NodeKind

/** Unified numeric typeface — uses system default for better legibility and elegance */
val NumericFont: FontFamily = FontFamily.Default

object NumStyle {
    val xl  = TextStyle(fontFamily = NumericFont, fontWeight = FontWeight.Bold, fontSize = 26.sp)
    val lg  = TextStyle(fontFamily = NumericFont, fontWeight = FontWeight.Bold, fontSize = 19.sp)
    val md  = TextStyle(fontFamily = NumericFont, fontWeight = FontWeight.SemiBold,      fontSize = 14.sp)
    val sm  = TextStyle(fontFamily = NumericFont, fontWeight = FontWeight.SemiBold,      fontSize = 12.sp)
    val xs  = TextStyle(fontFamily = NumericFont, fontWeight = FontWeight.Medium,  fontSize = 10.sp)
}

enum class AppTheme(@StringRes val labelRes: Int) {
    Light(com.prorf.app.R.string.theme_light),
    Dark(com.prorf.app.R.string.theme_dark),
    Carbon(com.prorf.app.R.string.theme_carbon)
}

data class KindColors(val col: Color, val tint: Color, val border: Color)

data class PrfColors(
    val isDark: Boolean,
    val bg: Color, val bg2: Color,
    val surf: Color, val surf2: Color, val surf3: Color,
    val prim: Color, val primDim: Color, val primTint: Color, val onPrim: Color,
    val sec: Color, val secTint: Color,
    val txt1: Color, val txt2: Color, val txt3: Color, val txt4: Color,
    val line: Color, val line2: Color,
    val ok: Color, val okTint: Color,
    val warn: Color, val warnTint: Color,
    val err: Color, val errTint: Color,
    val gold: Color, val goldTint: Color,
    val tx: KindColors, val loss: KindColors, val prop: KindColors,
    val rx: KindColors,
) {
    fun kind(k: NodeKind): KindColors = when (k) {
        NodeKind.TX -> tx
        NodeKind.LOSS -> loss
        NodeKind.PROPAGATION -> prop
        NodeKind.RX -> rx
    }
}

private fun c(hex: Long) = Color(hex)

val LightColors = PrfColors(
    isDark = false,
    bg = c(0xFFFAFBFC), bg2 = c(0xFFF1F3F6),
    surf = c(0xFFFFFFFF), surf2 = c(0xFFEDF0F5), surf3 = c(0xFFDEE3EB),
    prim = c(0xFF0064A4), primDim = c(0xFF004E90), primTint = c(0xFFD1E4FF), onPrim = c(0xFFFFFFFF),
    sec = c(0xFF006878), secTint = c(0xFFA2EEF8),
    txt1 = c(0xFF1A1C1E), txt2 = c(0xFF42474E), txt3 = c(0xFF72777F), txt4 = c(0xFFA0A7AF),
    line = c(0xFFC2C7CF), line2 = c(0xFFEDF0F5),
    ok = c(0xFF1B7B3A), okTint = c(0xFFE6F7EC),
    warn = c(0xFFC84B00), warnTint = c(0xFFFFF0E8),
    err = c(0xFFB71C1C), errTint = c(0xFFFFEBEE),
    gold = c(0xFFB25900), goldTint = c(0xFFFFF8EC),
    tx = KindColors(c(0xFF1565C0), c(0xFFEBF3FF), c(0xFFC2D9FF)),
    loss = KindColors(c(0xFFBF360C), c(0xFFFFF1EA), c(0xFFFFCAAD)),
    prop = KindColors(c(0xFF4A0E8F), c(0xFFF3E8FF), c(0xFFD5ADFF)),
    rx = KindColors(c(0xFF1B5E20), c(0xFFE8F8EA), c(0xFFAADAB2)),
)

val DarkColors = PrfColors(
    isDark = true,
    bg = c(0xFF12151C), bg2 = c(0xFF1A1E28),
    surf = c(0xFF202532), surf2 = c(0xFF2A3044), surf3 = c(0xFF343C54),
    prim = c(0xFF7EB8FF), primDim = c(0xFF559AEF), primTint = c(0xFF142A4E), onPrim = c(0xFF04182E),
    sec = c(0xFF40D8EE), secTint = c(0xFF082030),
    txt1 = c(0xFFE8ECF6), txt2 = c(0xFFA8B8D0), txt3 = c(0xFF687898), txt4 = c(0xFF3C4C64),
    line = c(0xFF2A3044), line2 = c(0xFF1E2438),
    ok = c(0xFF6FCF8A), okTint = c(0xFF0A2414),
    warn = c(0xFFFFAB55), warnTint = c(0xFF241400),
    err = c(0xFFF08080), errTint = c(0xFF220808),
    gold = c(0xFFFFAB55), goldTint = c(0xFF241400),
    tx = KindColors(c(0xFF82BAFF), c(0xFF0E1E3A), c(0xFF2A4878)),
    loss = KindColors(c(0xFFFFA060), c(0xFF221000), c(0xFF4A2800)),
    prop = KindColors(c(0xFFC48AFF), c(0xFF160828), c(0xFF3A1860)),
    rx = KindColors(c(0xFF72CC80), c(0xFF081C0A), c(0xFF1C4822)),
)

val CarbonColors = PrfColors(
    isDark = true,
    bg = c(0xFF060B18), bg2 = c(0xFF0C1426),
    surf = c(0xFF121E34), surf2 = c(0xFF1A2A46), surf3 = c(0xFF22345A),
    prim = c(0xFF3DD8F0), primDim = c(0xFF20B8D4), primTint = c(0xFF062028), onPrim = c(0xFF031018),
    sec = c(0xFFF0B840), secTint = c(0xFF1C1000),
    txt1 = c(0xFFE0EEF8), txt2 = c(0xFF8AACCC), txt3 = c(0xFF4A6888), txt4 = c(0xFF2A3C58),
    line = c(0xFF1A2A46), line2 = c(0xFF0E1A30),
    ok = c(0xFF44D86A), okTint = c(0xFF041404),
    warn = c(0xFFF0B840), warnTint = c(0xFF1C1000),
    err = c(0xFFFF5555), errTint = c(0xFF1A0404),
    gold = c(0xFFF0B840), goldTint = c(0xFF1C1000),
    tx = KindColors(c(0xFF44AAFF), c(0xFF061228), c(0xFF0E2A54)),
    loss = KindColors(c(0xFFFFA040), c(0xFF180C00), c(0xFF3A1C00)),
    prop = KindColors(c(0xFFAA70FF), c(0xFF0E0820), c(0xFF281848)),
    rx = KindColors(c(0xFF44D86A), c(0xFF041404), c(0xFF0C3018)),
)

val LocalPrf = staticCompositionLocalOf { LightColors }

object Prf {
    val colors: PrfColors
        @Composable get() = LocalPrf.current
}

@Composable
fun ProRFTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val prf = when (theme) {
        AppTheme.Light -> LightColors
        AppTheme.Dark -> DarkColors
        AppTheme.Carbon -> CarbonColors
    }
    val scheme = if (prf.isDark) {
        darkColorScheme(
            primary = prf.prim, onPrimary = prf.onPrim, primaryContainer = prf.primTint,
            secondary = prf.sec, background = prf.bg, surface = prf.surf,
            surfaceVariant = prf.surf2, onBackground = prf.txt1, onSurface = prf.txt1,
            onSurfaceVariant = prf.txt2, outline = prf.line, error = prf.err,
            surfaceContainerLow = prf.surf, surfaceContainer = prf.surf, surfaceContainerHigh = prf.surf2,
        )
    } else {
        lightColorScheme(
            primary = prf.prim, onPrimary = prf.onPrim, primaryContainer = prf.primTint,
            secondary = prf.sec, background = prf.bg, surface = prf.surf,
            surfaceVariant = prf.surf2, onBackground = prf.txt1, onSurface = prf.txt1,
            onSurfaceVariant = prf.txt2, outline = prf.line, error = prf.err,
            surfaceContainerLow = prf.surf, surfaceContainer = prf.surf, surfaceContainerHigh = prf.surf2,
        )
    }
    CompositionLocalProvider(LocalPrf provides prf) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
