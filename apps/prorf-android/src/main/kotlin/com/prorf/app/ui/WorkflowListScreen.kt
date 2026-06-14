package com.prorf.app.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prorf.app.data.WorkflowSummary
import com.prorf.app.viewmodel.WorkflowListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowListScreen(onOpenWorkflow: (String) -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val vm: WorkflowListViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application),
    )
    val state by vm.state.collectAsState()
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    if (deleteConfirmId != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = {
                Text("Delete workflow?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This cannot be undone.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = { vm.delete(deleteConfirmId!!); deleteConfirmId = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "ProRF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "RF Workflow Platform",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenWorkflow("new") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "New workflow")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "Loading…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.savedWorkflows.isNotEmpty()) {
                item { SectionHeader(title = "My Workflows", count = state.savedWorkflows.size) }
                items(state.savedWorkflows, key = { it.id }) { summary ->
                    WorkflowCard(
                        summary = summary,
                        isTemplate = false,
                        onClick = { onOpenWorkflow(summary.id) },
                        onDelete = { deleteConfirmId = summary.id },
                    )
                }
                item { Spacer(Modifier.height(4.dp)) }
                item { HorizontalDivider() }
                item { Spacer(Modifier.height(4.dp)) }
            }

            item { SectionHeader(title = "Templates", count = vm.templates.size) }
            if (vm.templates.isNotEmpty()) {
                items(vm.templates, key = { it.id }) { summary ->
                    WorkflowCard(
                        summary = summary,
                        isTemplate = true,
                        onClick = { onOpenWorkflow(summary.id) },
                        onDelete = null,
                    )
                }
            } else {
                item {
                    Text(
                        "No templates available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                }
            }

            if (state.savedWorkflows.isEmpty() && vm.templates.isEmpty()) {
                item {
                    EmptyState(modifier = Modifier.padding(top = 40.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(6.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun WorkflowCard(
    summary: WorkflowSummary,
    isTemplate: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val accentColor = if (isTemplate) MaterialTheme.colorScheme.secondary
                      else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(76.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                    .background(accentColor),
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                // Title row with last-modified time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = summary.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    val time = relativeTime(summary.lastModifiedMs)
                    if (time.isNotEmpty()) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                        )
                    }
                }

                Spacer(Modifier.height(5.dp))

                // Chain dots + counts row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NodeChainDots(nodeCount = summary.nodeCount, accentColor = accentColor)
                    Text(
                        text = buildString {
                            append(summary.nodeCount)
                            append(" node${if (summary.nodeCount != 1) "s" else ""}")
                            if (summary.edgeCount > 0) append(" · ${summary.edgeCount} edges")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                    )
                    if (isTemplate) TemplateBadge()
                }
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/** Small connected-dot visualization for the workflow node chain. */
@Composable
private fun NodeChainDots(nodeCount: Int, accentColor: Color) {
    if (nodeCount == 0) return
    val dots = nodeCount.coerceAtMost(6)
    val connectorColor = MaterialTheme.colorScheme.outlineVariant

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(dots) { i ->
            val dotColor = when {
                i == 0 -> Color(0xFF2F80ED)
                i == dots - 1 && nodeCount > 1 -> Color(0xFFEB5757)
                i % 3 == 1 -> Color(0xFF27AE60)
                else -> Color(0xFFF2994A)
            }
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(dotColor, CircleShape),
            )
            if (i < dots - 1) {
                Spacer(Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(6.dp)
                        .background(connectorColor),
                )
                Spacer(Modifier.width(2.dp))
            }
        }
        if (nodeCount > 6) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = "+${nodeCount - 6}",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontSize = 8.sp,
            )
        }
    }
}

@Composable
private fun TemplateBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = "Template",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "No workflows yet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap + to create your first RF workflow",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Formats a millisecond timestamp as a human-readable relative time string. */
private fun relativeTime(ms: Long): String {
    if (ms == 0L) return ""
    val diff = System.currentTimeMillis() - ms
    return when {
        diff < 60_000L -> "just now"
        diff < 3_600_000L -> "${diff / 60_000L}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000L}h ago"
        diff < 7 * 86_400_000L -> "${diff / 86_400_000L}d ago"
        else -> "${diff / (7 * 86_400_000L)}w ago"
    }
}
