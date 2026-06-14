package com.prorf.app.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.app.ProRfApp
import com.prorf.app.ui.theme.LocalProRfTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val application = LocalContext.current.applicationContext as Application
    val pluginRegistry = (application as? ProRfApp)?.pluginRegistry
    val nodeCount = pluginRegistry?.allDefinitions()?.size ?: 0
    val themeState = LocalProRfTheme.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // App branding header
            AppBrandHeader(nodeCount = nodeCount)

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SettingsSection(title = "Appearance") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Theme", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Switch to dark engineering view",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = themeState.isDarkTheme,
                            onCheckedChange = { themeState.toggleTheme() },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                SettingsSection(title = "About") {
                    SettingsRow(label = "Version", value = "0.1.0-alpha")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(label = "Platform", value = "ProRF Workflow Engine")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(label = "Architecture", value = "L0–L4 Module System")
                }

                Spacer(Modifier.height(16.dp))

                SettingsSection(title = "Capabilities") {
                    CapabilityRow(
                        label = "RF Domain",
                        description = "$nodeCount node types",
                        status = CapabilityStatus.ENABLED,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    CapabilityRow(
                        label = "Export PDF",
                        description = "Generate link budget reports",
                        status = CapabilityStatus.COMING_SOON,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    CapabilityRow(
                        label = "Cloud Sync",
                        description = "Sync workflows across devices",
                        status = CapabilityStatus.COMING_SOON,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    CapabilityRow(
                        label = "Monte Carlo Sweep",
                        description = "Statistical analysis of RF parameters",
                        status = CapabilityStatus.COMING_SOON,
                    )
                }

                Spacer(Modifier.height(16.dp))

                SettingsSection(title = "Data") {
                    SettingsRow(label = "Storage", value = "Local files")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(label = "Workflow format", value = "JSON v1")
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AppBrandHeader(nodeCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // App icon circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "RF",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = "ProRF",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "RF Workflow Computation Platform",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoChip(label = "$nodeCount nodes", color = MaterialTheme.colorScheme.primary)
                    InfoChip(label = "RF Domain", color = Color(0xFF27AE60))
                    InfoChip(label = "Offline", color = Color(0xFF9B51E0))
                }
            }
        }
    }
    HorizontalDivider()
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun InfoChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class CapabilityStatus { ENABLED, COMING_SOON, DISABLED }

@Composable
private fun CapabilityRow(label: String, description: String, status: CapabilityStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val (statusText, statusColor) = when (status) {
            CapabilityStatus.ENABLED -> "Active" to MaterialTheme.colorScheme.secondary
            CapabilityStatus.COMING_SOON -> "Soon" to MaterialTheme.colorScheme.outline
            CapabilityStatus.DISABLED -> "Off" to MaterialTheme.colorScheme.error
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusColor.copy(alpha = 0.12f),
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}
