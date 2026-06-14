package com.prorf.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prorf.app.data.WorkflowRepository
import com.prorf.app.data.WorkflowSummary
import com.prorf.domains.rf.WorkflowTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class WorkflowListState(
    val savedWorkflows: List<WorkflowSummary> = emptyList(),
    val isLoading: Boolean = true,
)

class WorkflowListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkflowRepository(application.filesDir)

    private val _state = MutableStateFlow(WorkflowListState())
    val state: StateFlow<WorkflowListState> = _state

    val templates: List<WorkflowSummary> = WorkflowTemplates.all.map { g ->
        WorkflowSummary(id = g.id, name = g.name, nodeCount = g.nodes.size, edgeCount = g.edges.size)
    }

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = WorkflowListState(
                savedWorkflows = repository.list(),
                isLoading = false,
            )
        }
    }

    fun delete(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
            reload()
        }
    }
}
