# ai-agents

Tool-agnostic AI agents with canonical YAML definitions and a Java 25 Spring
Boot renderer application for Codex, Claude, and Copilot.

## Repository Layout

- `agents/` contains the canonical agent definitions.
- `ai/PROJECT/DECISIONS/` contains the project ADRs that govern the catalog.
- `src/main/java/` contains the Java renderer and validation implementation.

## Canonical Agent Schema

Required fields:

- `id`
- `name`
- `purpose`
- `whenToUse`
- `boundaries`
- `prompt`

Optional v1 fields:

- `aliases`
- `examples`
- `toolHints`
- `notes`
- `preferredModels`
- `platformOverrides`

`preferredModels` is ordered from most preferred to least preferred. Each entry
uses:

```yaml
- provider: openai
  model: gpt-5.4
  reasoningEffort: high
```

Renderers scan the list from top to bottom and use the first compatible entry
for their target platform. If no compatible entry exists, the renderer falls
back to its built-in default.

## Build and Render

Use the Gradle wrapper:

```bash
./gradlew check
./gradlew renderAgents
```

Generated artifacts are written only under `build/rendered/`:

- Codex: `build/rendered/codex/.codex/agents/*.toml`
- Claude: `build/rendered/claude/.claude/agents/*.md`
- Copilot: `build/rendered/copilot/.github/agents/*.agent.md`

Generated target artifacts are build outputs only and are not committed to git.
