package com.prorf.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * L4 App Shell — top-level navigation.
 * Placeholder for M4 navigation wiring (Navigation Compose).
 * Will route between: WorkflowListScreen, WorkflowEditorScreen.
 */
@Composable
fun ProRfNavHost() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.WorkflowList) }
    when (currentScreen) {
        Screen.WorkflowList -> WorkflowListScreen(
            onOpenWorkflow = { currentScreen = Screen.WorkflowEditor(it) },
        )
        is Screen.WorkflowEditor -> WorkflowEditorScreen(
            workflowId = (currentScreen as Screen.WorkflowEditor).workflowId,
            onBack = { currentScreen = Screen.WorkflowList },
        )
    }
}

private sealed interface Screen {
    data object WorkflowList : Screen
    data class WorkflowEditor(val workflowId: String) : Screen
}
