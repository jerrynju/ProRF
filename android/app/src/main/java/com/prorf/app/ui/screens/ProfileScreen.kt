package com.prorf.app.ui.screens

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.prorf.app.BuildConfig
import com.prorf.app.R
import com.prorf.app.ui.components.SectionLabel
import com.prorf.app.ui.theme.AppTheme
import com.prorf.app.ui.theme.Prf

@Composable
fun ProfileScreen(
    theme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onClearData: () -> Unit = {},
) {
    val p = Prf.colors
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("prorf", Context.MODE_PRIVATE)
    var showAbout by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Current selections
    var langChoice by remember { mutableStateOf(prefs.getString("lang", "system") ?: "system") }

    Column(Modifier.fillMaxSize().background(p.bg).verticalScroll(rememberScrollState())) {
        // Avatar header
        Column(
            Modifier.fillMaxWidth().background(p.prim).padding(top = 28.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(72.dp).background(Color.White.copy(alpha = .2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("\uD83D\uDCE1", fontSize = 32.sp) }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.profile_name), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(stringResource(R.string.profile_caption), fontSize = 12.sp, color = Color.White.copy(alpha = .75f))
        }

        Column(Modifier.padding(16.dp)) {
            SectionLabel(stringResource(R.string.theme_style))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTheme.entries.forEach { t ->
                    val sel = theme == t
                    Box(
                        Modifier.weight(1f)
                            .background(if (sel) p.primTint else p.surf, RoundedCornerShape(12.dp))
                            .border(if (sel) 2.dp else 1.5.dp, if (sel) p.prim else p.line2, RoundedCornerShape(12.dp))
                            .clickable { onThemeChange(t) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(t.labelRes), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (sel) p.prim else p.txt2, textAlign = TextAlign.Center)
                    }
                }
            }

            SectionLabel(stringResource(R.string.other_settings))

            // Language row - clickable
            Row(
                Modifier.fillMaxWidth().clickable { showLangDialog = true }.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.setting_language), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = p.txt1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when (langChoice) {
                            "en" -> stringResource(R.string.language_english)
                            "zh" -> stringResource(R.string.language_chinese)
                            else -> stringResource(R.string.setting_language_value)
                        },
                        fontSize = 13.sp, color = p.txt3,
                    )
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = p.txt4, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = p.line2)

            // Units row - display only (SI only; other units not yet implemented)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.setting_units), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = p.txt2)
                Text(stringResource(R.string.units_si), fontSize = 13.sp, color = p.txt4)
            }
            HorizontalDivider(color = p.line2)

            // Auto save row (display only - always on)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.setting_save_mode), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = p.txt1)
                Text(stringResource(R.string.setting_on), fontSize = 13.sp, color = p.txt3)
            }
            HorizontalDivider(color = p.line2)

            // About row - clickable to show dialog
            Row(
                Modifier.fillMaxWidth().clickable { showAbout = true }.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.setting_about), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = p.txt1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("v${BuildConfig.VERSION_NAME}", fontSize = 13.sp, color = p.txt3)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = p.txt4, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = p.line2)

            // Help row - clickable to show usage guide
            Row(
                Modifier.fillMaxWidth().clickable { showHelp = true }.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.help_title), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = p.txt1)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = p.txt4, modifier = Modifier.size(18.dp))
            }
            HorizontalDivider(color = p.line2)

            // Clear data row - danger action
            Row(
                Modifier.fillMaxWidth().clickable { showClearDialog = true }.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.clear_data_title), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = p.err)
            }
            HorizontalDivider(color = p.line2)

            Spacer(Modifier.height(24.dp))
        }
    }

    // Language selection dialog
    if (showLangDialog) {
        val options = listOf(
            "system" to stringResource(R.string.language_system),
            "en" to stringResource(R.string.language_english),
            "zh" to stringResource(R.string.language_chinese),
        )
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text(stringResource(R.string.language_dialog_title)) },
            text = {
                Column {
                    options.forEach { (value, label) ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                langChoice = value
                                prefs.edit().putString("lang", value).apply()
                                applyLocale(context, value)
                                showLangDialog = false
                            }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = langChoice == value, onClick = {
                                langChoice = value
                                prefs.edit().putString("lang", value).apply()
                                applyLocale(context, value)
                                showLangDialog = false
                            })
                            Spacer(Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp, color = p.txt1)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.about_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.about_description),
                        color = p.txt2,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text(stringResource(R.string.action_ok)) }
            }
        )
    }

    if (showHelp) {
        val sections = listOf(
            stringResource(R.string.help_nodes_title) to stringResource(R.string.help_nodes_body),
            stringResource(R.string.help_metrics_title) to stringResource(R.string.help_metrics_body),
            stringResource(R.string.help_tips_title) to stringResource(R.string.help_tips_body),
        )
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(stringResource(R.string.help_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.help_intro), fontSize = 13.sp, color = p.txt2, lineHeight = 18.sp)
                    sections.forEach { (title, body) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.txt1)
                            Text(body, fontSize = 12.sp, color = p.txt2, lineHeight = 17.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text(stringResource(R.string.action_ok)) }
            },
        )
    }

    // Clear data confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_data_title)) },
            text = { Text(stringResource(R.string.clear_data_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearData()
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/** Apply app locale change. */
private fun applyLocale(context: Context, choice: String) {
    val localeTag = when (choice) {
        "en" -> "en"
        "zh" -> "zh"
        else -> ""  // system default
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val lm = context.getSystemService(LocaleManager::class.java)
        lm.applicationLocales = if (localeTag.isEmpty()) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(localeTag)
        // UX5: Android 13+ auto-refreshes activity via LocaleManager, no explicit recreate needed.
    } else {
        AppCompatDelegate.setApplicationLocales(
            if (localeTag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(localeTag)
        )
        // UX5: On pre-TIRAMISU devices, AppCompatDelegate may not immediately recreate all
        // activities. Force a recreate so ProfileScreen and backstack activities refresh the UI
        // right away, matching user expectations of "instant" language switching.
        (context as? android.app.Activity)?.recreate()
    }
}
