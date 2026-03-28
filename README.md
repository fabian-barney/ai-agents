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

Provider ids and model ids are represented in code as typed value objects
(`ProviderId` and `ModelId`) instead of raw strings.

## Build and Render

Use the Gradle wrapper:

```bash
./gradlew check
./gradlew crap4javaCheck
./gradlew renderAgents
```

`./gradlew crap4javaCheck` runs the pinned upstream `crap4java` tool against
the repository's production Java sources. The first run downloads the pinned
upstream commit into `build/crap4java/`, patches it for this Gradle build, and
compiles the helper jar locally before executing the gate.

Renderer output artifacts from `./gradlew renderAgents` are written under `build/rendered/`:

- Codex: `build/rendered/codex/.codex/agents/*.toml`
- Claude: `build/rendered/claude/.claude/agents/*.md`
- Copilot: `build/rendered/copilot/.github/agents/*.agent.md`

The `crap4javaCheck` helper cache lives under `build/crap4java/`.

Generated target artifacts are build outputs only and are not committed to git.

## Renderer Configuration

Renderer defaults and emitted field names are configurable through
`catalog.renderers.*`.

Examples:

- `catalog.renderers.codex.default-model`
- `catalog.renderers.codex.compatible-provider`
- `catalog.renderers.codex.output-directory`
- `catalog.renderers.claude.tools-key`
- `catalog.renderers.copilot.model-key`
