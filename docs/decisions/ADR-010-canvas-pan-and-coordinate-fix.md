# ADR-010: Canvas Pan + Coordinate System Fix

**Date:** 2026-06-13  
**Status:** Accepted  
**Milestone:** M7 – Canvas UX & Correctness

---

## Context

Three related bugs were found in the L2 UI canvas layer during post-M6 review:

1. **Drag pixel/dp mismatch** – `detectDragGestures` provides `dragAmount` in screen pixels, but node positions (`card.x`, `card.y`) are stored as dp-unit floats. Adding raw pixels to dp values causes nodes to move 2–3× faster than the finger on common 2×/3× density devices (1× emulators happened to mask this because px == dp at density=1.0).

2. **Edge rendering pixel/dp mismatch** – The `Canvas` composable draws in screen pixels, but `EdgeOverlay` used dp-unit node positions directly as pixel coordinates. Edges were misaligned on any device with density ≠ 1.0.

3. **StatusDot invisible** – `Canvas(modifier = Modifier)` has no minimum size; the composable reported a zero-size layout on some devices and drew nothing.

Additionally, M2 had explicitly deferred canvas pan ("no gesture handling yet"), leaving no way to reach nodes that drift off-screen during drag.

---

## Decision

### Coordinate convention (clarified)

Node positions (`NodePosition.x/y`) are **dp-as-Float**. All uses must respect this:

| Operation | Correct |
|---|---|
| Render position | `Modifier.offset(x.dp, y.dp)` |
| Canvas draw (pixels) | `x * density + panOffsetPx.x` |
| Drag update | `x + dragAmountPx / density` |

### Canvas pan

`WorkflowCanvas` owns a `panOffsetPx: Offset` state (screen pixels, not persisted). It is:
- Updated by `detectDragGestures` on the outer `Box` (background pan)
- Added to node display positions: `displayX = card.x + panOffsetPx.x / density`
- Added to Canvas drawing coordinates: `fromXPx = from.x * density + panOffsetPx.x`

### Gesture priority

Compose dispatches pointer events to children before parents. When a user drags a node, the node's `detectDragGestures` consumes the position change (via internal `consumePositionChange()`), and the parent's `awaitTouchSlopOrCancellation` sees `change.isConsumed == true` and returns null — the canvas background drag never fires. Dragging empty space reaches only the parent.

### StatusDot fix

`Canvas(modifier = Modifier.size(8.dp))` with `drawCircle(radius = size.minDimension / 2f)` ensures a visible 8dp dot on all displays.

### Inspector scroll

Wrapped the Inspector `Column` in `verticalScroll(rememberScrollState())` so nodes with many parameters don't clip content on small screens.

---

## Consequences

**Good:**
- Node dragging is now pixel-accurate on all display densities.
- Edge bezier curves connect correctly at the right-center port of the source node and left-center port of the target node.
- Users can pan the canvas to reach off-screen nodes.
- StatusDot renders reliably.
- Inspector content is always accessible via scroll.

**Bad / Deferred:**
- Pan state (`panOffsetPx`) resets to zero when the composable leaves composition (e.g. navigating away and back). Persisting view transform is deferred.
- Pinch-to-zoom is not included; canvas scale remains 1×. Deferred to M8.
- The "background pan while touching a node" conflict relies on Compose's undocumented-but-observed child-first dispatch order. A future refactor may use explicit gesture coordination.

---

## Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| Store positions in pixels | ViewModel (`addNode`) uses density-agnostic dp values; importing `LocalDensity` into ViewModel would violate Android architecture guidelines |
| `graphicsLayer` transform for pan | Doesn't affect layout bounds → hit-testing mismatches under pan |
| Two-finger-only pan via `detectTransformGestures` | More discoverable UX than requiring two fingers for basic navigation |
| Shift all node positions for pan | Permanently mutates graph state for a transient view operation |
