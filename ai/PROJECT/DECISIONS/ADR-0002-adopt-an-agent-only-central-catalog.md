# ADR-0002 Adopt an agent-only central catalog

- Status: Accepted
- Date: 2026-03-28

## Context
This repository is intended to define reusable agents that can be rendered into
multiple host environments. Earlier design exploration considered central
registries for capabilities, environments, skills, shared instruction
fragments, and shared output contracts.

That broader model would add schema and maintenance overhead before the project
has evidence that those extra layers are necessary. The Codex subagent model
also aligns better with narrow, explicit agents that carry their own behavior
and are invoked intentionally for bounded work.

## Decision
- The central repository will own only canonical agent definitions and
  renderer/build logic.
- Version 1 will not centrally model capabilities, environments, skills, shared
  instruction fragments, shared output contracts, or orchestration envelopes.
- Each canonical agent definition must be self-contained and include its own
  role, behavioral boundaries, and built-in prompt/instructions.
- Project-local instruction files, installed skills, tool integrations, and
  other runtime context remain outside the central catalog and are resolved by
  the host environment at runtime.
- The `orchestrator` agent will stay thin and focus on delegation and result
  synthesis. Specialist behavior belongs in specialist agents.

## Consequences
- The repository can stay small and focused on `agents/` plus renderer/build
  logic.
- Renderers adapt one canonical agent definition into each supported platform's
  custom-agent artifact.
- Runtime behavior may vary slightly across host environments because execution
  details are resolved dynamically instead of being modeled centrally.
- If repeated implementation pain appears, explicit capability or environment
  layers can be introduced later through a new ADR.
