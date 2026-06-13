# ADR-008 — M5-step3: Edge Deletion UI

## Status
Accepted — 2026-06-13

## Context
After M5-step2 landed the two-tap edge creation flow, users had no way to remove edges
they created. Completing the basic canvas manipulation set required edge deletion.

Two approaches were considered:

**Option A — Tap-on-edge on the canvas.**
Edges are drawn as bezier curves on a raw Compose `Canvas`. Making them tappable requires
per-frame hit-testing of parametric bezier paths, is complex to implement correctly, and
adds significant pointer-input logic to a composable that is explicitly forbidden from
holding state. It would also require the canvas to distinguish a drag (node move) from a
tap on a narrow curve, which introduces subtle UX regressions.

**Option B — Delete via Inspector Connections section (chosen).**
When a node is selected, the Inspector already shows its metadata. Adding a "Connections"
section that lists incoming/outgoing edges with a trash icon per row is:
- Zero new pointer-input math
- Consistent with the existing Inspector interaction model (parameters, outputs)
- Clear: the user sees exactly which edge connects to which node label and can remove it
- Keeps L2 UI layer clean — uses `UiEdgeRow` (a new L2 model type) not `Edge` directly

## Decision
Edge deletion is exposed via the Inspector, not the canvas.

New L2 model `UiEdgeRow(edgeId, otherNodeLabel, direction: INCOMING|OUTGOING)` is computed
in `WorkflowEditorScreen` from `graph.edges` filtered by the selected node. The Inspector
receives `connectedEdges: List<UiEdgeRow>` and `onEdgeDeleteRequested: (edgeId) -> Unit`.

`WorkflowEditorViewModel.deleteEdge(edgeId)` filters the edge list, auto-saves, and
re-runs the DAG — the same pattern used by `finishConnecting`.

## Consequences
- Inspector now has five sections: Inputs, Parameters, Outputs, Diagnostics, **Connections**
- The "Connect to node →" button remains below the Connections section
- Bezier canvas hit-testing deferred to a future milestone if demand arises
- `UiEdgeRow` lives in `ui/model/` alongside `UiNodeCard` — consistent placement
- 3 new tests in `EdgeDeletionTest` verify the delete→re-execute cycle

## Alternatives Rejected
- Long-press on node to show a "Disconnect all" action — too blunt, can't target specific edges
- Tap-on-edge — bezier hit-testing complexity not justified at this stage
