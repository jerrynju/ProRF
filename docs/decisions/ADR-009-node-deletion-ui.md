# ADR-009: Node Deletion UI (M6-step1)

## Status
Accepted — 2026-06-13

## Context
After M5-step3 landed edge deletion, the natural next step is to allow users to remove an entire node from the workflow. Deleting a node must also silently remove every edge that references it (both incoming and outgoing), otherwise the graph would contain dangling edge references that would crash the DAG sort.

## Decision
- Added `deleteNode(nodeId: String)` to `WorkflowEditorViewModel`. It filters the node **and** all incident edges out of the graph, clears the selection if the deleted node was selected, auto-saves, and re-runs the DAG.
- Added an optional `onNodeDeleteRequested: (() -> Unit)?` callback to `Inspector` (L2 UI layer). When provided, a destructive "Delete Node" button is shown at the bottom of the inspector panel using `errorContainer` colours to signal irreversibility.
- `WorkflowEditorScreen` always passes `onNodeDeleteRequested = { vm.deleteNode(selectedNode.id) }`.

## Rationale
- **Atomicity**: removing edges alongside the node in a single graph copy prevents an intermediate invalid state where an edge points to a missing node.
- **Placement in Inspector**: the Inspector already owns the "per-node actions" surface (parameter editing, connect, edge deletion). Adding the node-delete button there keeps all node actions in one place and avoids a floating context menu.
- **Colour signal**: `errorContainer` / `onErrorContainer` communicates the destructive nature without a confirmation dialog — consistent with edge deletion UX (M5-step3).
- **No undo**: out of scope for this milestone. The user can re-add the node if needed.

## Alternatives Considered
- **Long-press on canvas node**: requires gesture detection on `WorkflowCanvas`; out of scope for now.
- **Confirmation dialog**: adds friction; the Inspector already requires the user to explicitly tap a node to reach this button — considered sufficient guard.

## Consequences
- Deleting a source node (e.g. `SignalSource`) will leave downstream nodes with no input, but execution still completes — they compute from their defaults. This is correct and harmless.
- The `Inspector` composable gains one new optional parameter. All existing call sites pass `null` by default, so no regressions.

## Tests
`NodeDeletionTest` (4 cases):
1. Deleted node absent from execution outputs; upstream unaffected.
2. Incident edges removed alongside the node.
3. Downstream propagation breaks after middle-node deletion.
4. No-op on unknown node id.
