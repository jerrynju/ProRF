# ADR-011: Node Rename UI (M6-step2)

**Date:** 2026-06-13  
**Status:** Accepted  
**Context:** Completing the node lifecycle CRUD — create (addNode), read (Inspector), update (rename + move + edit params), delete.

---

## Context

After M6-step1 (node deletion), nodes could be created, repositioned, have their parameters edited, and be deleted. However the only display name was derived from `typeId.substringAfterLast('.')` — e.g. `amplifier` — with no way for the user to give a meaningful label like "Front-end LNA". In a chain of multiple amplifiers this creates ambiguity on the canvas.

---

## Decision

Add an optional `label: String?` field to `NodeInstance` (L0 platform layer). This is a pure runtime property with no domain semantics — appropriate for L0.

- **NodeInstance** gains `val label: String? = null`. `null` means "use the type-derived default".
- **WorkflowEditorViewModel** — `renameNode(nodeId, newLabel)` sets the label (`ifBlank → null` so an empty field reverts to type-derived display name). The `uiCards` computation uses `node.label ?: node.typeId.substringAfterLast('.')`.
- **Inspector** — gains `onNodeRenameRequested: ((String) -> Unit)?`. When provided, the static title text is replaced by an `OutlinedTextField` (pre-filled with current label or type name). Changes commit on IME Done or focus loss.
- **WorkflowEditorScreen** — wires `onNodeRenameRequested = { vm.renameNode(selectedNode.id, it) }`.
- **Serialization** — `NodeDocument` gains `val label: String? = null`. Serializer passes label through; old documents without the field decode as `null` (backward compatible — no schema version bump needed).

---

## Alternatives Considered

**A: Store label as a special parameter `_label` in `parameters: Map<String, Any>`**  
Rejected. Mixing UI metadata into the domain parameter map violates the separation between execution data and display state. The platform graph model is the right home.

**B: Separate `WorkflowNodeLabel` table / side-map**  
Rejected for MVP. Extra indirection with no benefit at this scale.

**C: Edit via a separate rename dialog**  
Deferred. An inline field in the Inspector has lower tap cost and no extra screen state. A dialog variant can be added later if UX testing shows the inline field is awkward on small screens.

---

## Risks

- The `OutlinedTextField` commits on focus loss, which can trigger a rename when the user just taps away. Mitigated by treating `ifBlank { null }` so a cleared field restores the default name silently.
- Schema compatibility: new `label` field is optional with default `null`, so old JSON loads without migration.

---

## Verification

- `platform/src/test/.../NodeRenameTest.kt` — 5 tests covering label set, parameter preservation, clear-to-null, blank treatment, default null.
- `serialization/src/test/.../WorkflowSerializerTest.kt` — 2 new tests: label round-trip, null label deserialization.
- Static check: no RF layer references added; label field is purely in L0 + L4 wiring.
