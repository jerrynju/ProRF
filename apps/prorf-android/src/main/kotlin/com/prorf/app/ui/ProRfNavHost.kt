package com.prorf.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GridView
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * L4 App Shell — top-level navigation host.
 *
 * 5-tab bottom nav: 流向 | 节点库 | 模板 | 结果 | 设置.
 * WorkflowEditor is full-screen and replaces the Scaffold entirely.
 */
@Composable
fun ProRfNavHost() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.WorkflowList) }

    // Editor is full-screen — no bottom nav
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
                    label = "流向",
                    selected = currentScreen == Screen.WorkflowList,
                    onClick = { currentScreen = Screen.WorkflowList },
                )
                BottomNavItem(
                    icon = Icons.Default.Extension,
                    label = "节点库",
                    selected = currentScreen == Screen.Library,
                    onClick = { currentScreen = Screen.Library },
                )
                BottomNavItem(
                    icon = Icons.Default.GridView,
                    label = "模板",
                    selected = currentScreen == Screen.Templates,
                    onClick = { currentScreen = Screen.Templates },
                )
                BottomNavItem(
                    icon = Icons.Default.BarChart,
                    label = "结果",
                    selected = currentScreen == Screen.Analysis,
                    onClick = { currentScreen = Screen.Analysis },
                )
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "设置",
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
                Screen.Templates -> TemplatesScreen(
                    onOpenWorkflow = { currentScreen = Screen.WorkflowEditor(it) },
                )
                Screen.Analysis -> AnalysisScreen()
                Screen.Settings -> SettingsScreen()
                is Screen.WorkflowEditor -> Unit
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
        label = {
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
    )
}

private sealed interface Screen {
    data object WorkflowList : Screen
    data object Library : Screen
    data object Templates : Screen
    data object Analysis : Screen
    data object Settings : Screen
    data class WorkflowEditor(val workflowId: String) : Screen
}
