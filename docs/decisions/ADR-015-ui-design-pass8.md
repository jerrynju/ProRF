# ADR-015: UI Design Pass 8 — Edge Value Labels & Visual Refinements

## Status
Accepted

## Context

Seven prior UI design passes built the core visual language: node cards with category color stripes, bezier edge curves, the Inspector with QuickSummaryBanner, ChainListView with step numbers, and SignalChainChart. The canvas was the weakest area: edges showed direction via arrowheads but carried no value information, making it impossible to trace signal levels visually without switching to the chain list view.

Design pass 8 addresses three gaps:

1. **No signal values on canvas edges** — an RF link budget tool's primary purpose is tracking signal levels through each connection. Engineers could only see values on node cards (output of the node), not _on_ the connecting lines.

2. **Generic "Signal Chain" title** — the chain list header showed a static label rather than the actual workflow name, breaking context when switching between multiple workflows.

3. **Library grid too narrow for descriptions** — the 2-column grid compressed node description text, making the Library screen hard to use for discovering what each node type does.

## Decisions

### A. Edge value labels in WorkflowCanvas

`WorkflowCanvas` now accepts `executionOutputs: Map<String, Map<String, Any>>`. In the edges draw layer, after each bezier curve and arrowhead, the primary output of the source node is drawn as a small text label at the bezier midpoint.

Implementation:
- Midpoint at t=0.5 for symmetric control points simplifies to `(start + end) / 2`.
- Label only shown when edge length > 48dp (prevents overlapping labels on short/stacked edges).
- Background: opaque surface-tinted rounded rect (`surface.copy(alpha=0.94f)`) drawn before text so the label is legible over the edge curve.
- Text color matches the source node's category color at full opacity.
- Text paint and text bounds rect are `remember`'d to avoid per-frame allocation.

### B. Selection glow layer (Layer 2.5)

A soft blue rounded rect (primary at 10% alpha) is drawn above edges but below node cards for selected nodes. This gives canvas-level spatial confirmation of which node the Inspector is showing, without relying solely on the node card's 2dp border change.

### C. WorkflowCanvas wired with executionOutputs

`WorkflowEditorScreen` now passes `s.executionOutputs` to `WorkflowCanvas`. Previously this map was only passed to the Inspector and ChainListView.

### D. Workflow name in chain list header

`ChainListView` accepts `workflowName: String?`. `ChainListHeader` shows `workflowName ?: "Signal Chain"`. The actual workflow name from `WorkflowGraph.name` is passed through from the editor state.

### E. Library screen — single-column grouped list

The 2-column grid is replaced with a grouped single-column layout:
- Categories are displayed as section headers with colored dot + count badge.
- Nodes are grouped in Card containers (one card per category) with dividers.
- Each node row: 42dp category circle icon (left) + display name + description (center) + port/param badges (right column).
- Description (up to 2 lines) is fully visible at reasonable text size.
- Search also filters on `description` text now (was name/typeId only).

### F. Inspector improvements

- **ParametersTab**: Shows a parameter count header + divider before the editor.
- **ResultsTab**: Shows an output count header before result cards.
- **ConnectionsTab**: Connections are now grouped into INCOMING / OUTGOING sections with colored section labels and directional arrow badges (`→`, `←`) per row, colored by port type (primary for outgoing, secondary for incoming).

## Alternatives Considered

- **Tooltip on hover instead of persistent label**: Not applicable on mobile (no hover).
- **Edge label in a separate Compose overlay** (instead of native canvas text): Would require matching position calculation in two coordinate systems; native canvas text in DrawScope is simpler and avoids layout passes.
- **Keep 2-column library grid**: Descriptions were truncated at 2 lines in 50% of screen width — unreadable for longer RF node descriptions like FSPL formula descriptions.

## Consequences

- Canvas becomes more visually rich; engineers can trace signal values without switching views.
- `WorkflowCanvas` API gains `executionOutputs` parameter — callers that don't have outputs pass the default `emptyMap()`.
- Two remembered `android.graphics.Paint` objects per `WorkflowCanvas` instance — negligible memory cost.
- Library browsing is significantly improved for new users discovering node types.
