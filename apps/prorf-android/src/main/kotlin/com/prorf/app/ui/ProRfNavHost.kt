package com.prorf.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * L4 App Shell — top-level navigation host.
 *
 * WorkflowEditor occupies the full screen (no bottom nav).
 * All other destinations show the bottom navigation bar.
 */
@Composable
fun ProRfNavHost() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.WorkflowList) }

    // Editor is always full-screen — skip the Scaffold/bottom bar entirely
    if (currentScreen is Screen.WorkflowEditor) {
        WorkflowEditorScreen(
            workflowId = (currentScreen as Screen.WorkflowEditor).workflowId,
            onBack = { currentScreen = Screen.WorkflowList },
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem(
                    icon = Icons.Default.AccountTree,
                    label = "Workflows",
                    selected = currentScreen == Screen.WorkflowList,
                    onClick = { currentScreen = Screen.WorkflowList },
                )
                BottomNavItem(
                    icon = Icons.Default.Extension,
                    label = "Library",
                    selected = currentScreen == Screen.Library,
                    onClick = { currentScreen = Screen.Library },
                )
                BottomNavItem(
                    icon = Icons.Default.BarChart,
                    label = "Analysis",
                    selected = currentScreen == Screen.Analysis,
                    onClick = { currentScreen = Screen.Analysis },
                )
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    selected = currentScreen == Screen.Settings,
                    onClick = { currentScreen = Screen.Settings },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                Screen.WorkflowList -> WorkflowListScreen(
                    onOpenWorkflow = { currentScreen = Screen.WorkflowEditor(it) },
                )
                Screen.Library -> LibraryScreen()
                Screen.Analysis -> AnalysisScreen()
                Screen.Settings -> SettingsScreen()
                is Screen.WorkflowEditor -> Unit // handled above
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
    )
}

private sealed interface Screen {
    data object WorkflowList : Screen
    data object Library : Screen
    data object Analysis : Screen
    data object Settings : Screen
    data class WorkflowEditor(val workflowId: String) : Screen
}
