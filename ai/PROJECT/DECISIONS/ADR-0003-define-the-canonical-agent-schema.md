# ADR-0003 Define the canonical agent schema

- Status: Accepted
- Date: 2026-03-28

## Context
The project needs a single source format for authoring agents before generating
platform-specific artifacts. That schema must stay small, support embedded
agent instructions, and leave only narrow platform-specific differences to the
renderers.

One additional requirement is explicit model preference. The canonical agent
definition should express which models are preferred without forcing a separate
model-abstraction layer.

## Decision
- The canonical agent definition will require these fields:
  - `id`
  - `name`
  - `purpose`
  - `whenToUse`
  - `boundaries`
  - `prompt`
- The canonical agent definition may additionally include:
  - `aliases`
  - `examples`
  - `toolHints`
  - `notes`
  - `preferredModels`
  - `platformOverrides`
- `preferredModels` is a precedence-ordered list from most preferred to least
  preferred. Entries are evaluated in order, and a renderer should use the
  first compatible entry for its target platform.
- `preferredModels` entries may carry only the data needed for deterministic
  renderer selection, such as provider, concrete model identifier, and optional
  execution hints like reasoning effort.
- If no preferred model is compatible with a given renderer target, the
  renderer falls back to its target-specific default behavior.
- `platformOverrides` must stay small and cover only true renderer-specific
  differences, such as Codex sandbox defaults or target-specific tool
  restrictions.
- Generated artifacts must embed the agent's own prompt/instructions rather
  than depend on a shared central instructions layer.

## Consequences
- Agent authors get one stable authoring format for all supported renderers.
- Model selection becomes explicit and deterministic without introducing a
  separate shared model-profile system.
- Renderers remain responsible for compatibility checks, fallback behavior, and
  mapping the canonical prompt into platform-specific fields.
- Schema changes that alter required fields or override semantics should be
  captured in a future ADR.
