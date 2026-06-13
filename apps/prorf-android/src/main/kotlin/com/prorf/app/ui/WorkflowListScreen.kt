package com.prorf.app.ui

import android.app.Application
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("ProRF Workflows") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onOpenWorkflow("new") }) {
                Icon(Icons.Default.Add, contentDescription = "New workflow")
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading…")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.savedWorkflows.isNotEmpty()) {
                item {
                    SectionLabel("My Workflows")
                }
                items(state.savedWorkflows, key = { it.id }) { summary ->
                    WorkflowCard(
                        summary = summary,
                        onClick = { onOpenWorkflow(summary.id) },
                        onDelete = { vm.delete(summary.id) },
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
                item { HorizontalDivider() }
                item { Spacer(Modifier.height(8.dp)) }
            }

            item {
                SectionLabel("Templates")
            }
            items(vm.templates, key = { it.id }) { summary ->
                WorkflowCard(
                    summary = summary,
                    onClick = { onOpenWorkflow(summary.id) },
                    onDelete = null,
                )
            }

            if (state.savedWorkflows.isEmpty() && vm.templates.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No workflows yet", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Tap + to create your first RF link budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun WorkflowCard(
    summary: WorkflowSummary,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${summary.nodeCount} node(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
