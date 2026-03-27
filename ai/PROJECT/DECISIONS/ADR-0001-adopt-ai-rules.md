# ADR-0001 Adopt ai-rules

- Status: Accepted
- Date: 2026-03-28

## Context
This repository needs a shared AI instruction baseline that can be updated
independently from project-specific guidance. The project also needs a stable
place to keep local rules, lessons learned, and architecture decisions outside
the vendored baseline.

## Decision
- Vendor `ai-rules` under `ai/AI-RULES` using a git subtree.
- Keep project-specific AI guidance under `ai/PROJECT`.
- Point `AGENTS.md`, `CLAUDE.md`, and `.github/copilot-instructions.md` at the
  baseline and downstream entry points.

## Consequences
- Baseline updates will use `git subtree pull --prefix ai/AI-RULES ... --squash`.
- Project-specific AI guidance remains tracked without modifying vendor files.
- Lessons learned and ADRs stay outside the vendored subtree, so updates do not
  overwrite them.
