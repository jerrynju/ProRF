# ADR-006: M5 — Parameter Editing with Live Re-Run

## Status
Accepted (2026-06-13)

## Context
M4 wired the Inspector panel to display node parameters, but `onParameterChanged` was a no-op (`{ _, _ -> /* TODO */ }`). ParameterEditor (L2) was already built with text fields and value parsing, but not connected to any write path.

## Decision
Wire parameter editing end-to-end:

1. **Inspector (L2)** now renders `ParameterEditor` in the Parameters section instead of read-only rows. The `onParameterChanged` callback passes `(key, value)` to the caller — Inspector has no knowledge of the engine.

2. **WorkflowEditorViewModel (L4)** adds `updateParameter(nodeId, key, value)` that:
   - Immutably updates the node parameter in `WorkflowGraph`
   - Auto-saves to disk (existing `autoSave`)
   - Re-runs `DagExecutionEngine` on a background coroutine — result state flows back via `StateFlow`

3. **WorkflowEditorScreen (L4)** threads `selectedNode.id` into the callback so the ViewModel receives a fully-qualified `(nodeId, key, value)` triple.

## Alternatives Considered
- **Debounced re-run**: Wait 300ms after the last keystroke before re-running. Rejected: adds latency, premature optimization for a first cut. Revisit in M6 if performance is an issue.
- **Explicit Apply button**: User triggers re-run manually. Rejected: breaks the live feedback loop that makes the inspector useful.

## Consequences
- Every keystroke in a parameter field triggers a full DAG re-run. Acceptable for current node counts (<20). For large graphs, debounce or incremental recompute can be added later without changing the ViewModel contract.
- Inspector no longer needs to know the current execution outputs for parameter rendering — they come from ViewModel state and are passed as `executionOutputs`.
- `ParameterEditor` remains pure L2 (no domain imports, no executor calls). Layer boundary intact.
