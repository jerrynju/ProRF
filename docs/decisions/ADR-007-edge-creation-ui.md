# ADR-007: Edge Creation UI (M5-step2)

## Status
Accepted — 2026-06-13

## Context
After M5-step1 (parameter editing), nodes can be added to the canvas and their parameters edited live. However, there was no way for a user to connect nodes with edges through the UI. Edges were only present via pre-built templates. This blocked any user-constructed workflow.

## Decision
Implement a two-tap edge creation flow:
1. User selects a source node → Inspector shows "Connect to node →" button.
2. Tapping the button enters **connect mode** — the canvas shows a banner and a dashed highlight around the source node.
3. User taps any target node → edge is created automatically using the first output port of the source and the first input port of the target.
4. DAG re-runs and auto-save fires immediately.
5. Cancel button in the TopAppBar exits connect mode without creating an edge.

## Rationale
### Why two-tap instead of drag-from-port?
Drag-from-port requires hit-testing on precise port positions, which is unreliable on a canvas where node coordinates are stored as bare floats (not accounting for screen density scaling). The two-tap approach:
- Requires zero port coordinate calculation
- Works correctly regardless of zoom/pan state
- Is more accessible on small screens

### Why first-output → first-input port matching?
All RF nodes in M3 are serial-chain nodes with exactly one RF input and one RF output. More than one port per node is not used in any current template. Picking `firstOrNull()` is the MVP rule; future work can add a port picker dialog when nodes gain multiple compatible ports.

### Why check for duplicate edges in the ViewModel?
The DAG engine is resilient to duplicate edges (the second one overwrites the first input binding), but duplicates create visual noise on the canvas. Filtering at the ViewModel level prevents silent state pollution without requiring any change to the platform layer.

## Alternatives Considered
- **Drag-from-port**: More conventional but fragile given the current coordinate system mismatch (dp vs pixels in the canvas overlay). Deferred to a future canvas refactor.
- **Dialog-based port picker**: Premature for M5. Only one port type exists per node in the current RF domain pack.
- **Edge deletion UI**: Not in this task. Edges can be removed by discarding the workflow and starting from a template.

## Consequences
- Inspector grows one optional parameter (`onConnectRequested`). Callers that don't pass it see no change.
- `WorkflowEditorState.Ready` gains `connectingFromNodeId: String?`. Serialization is not affected (state is transient, not persisted).
- Canvas banner occupies ~32dp at the top during connect mode; does not affect node positions.
- 4 new tests in `EdgeCreationTest.kt` verify the graph-level correctness of incremental edge addition.
