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

The project coordinates are `media.barney:ai-agents`.

Use the Gradle wrapper:

```bash
./gradlew check
./gradlew crap-java-check
./gradlew cognitive-java-check
./gradlew qualityGate
./gradlew renderAgents
```

`./gradlew crap-java-check` runs the shared `media.barney.crap-java` `0.5.0`
gate against the repository's production Java sources. `./gradlew cognitive-java-check`
runs the shared `media.barney.cognitive-java` `0.4.0` gate. `./gradlew check`
now includes the cognitive gate, and `./gradlew qualityGate` remains the
repo-local convenience entrypoint that runs `check` plus the dedicated
`crap-java-check` and `cognitive-java-check` tasks.

Gradle resolves the shared `crap-java` and `cognitive-java` plugins from
published artifacts using `mavenCentral()` first and `gradlePluginPortal()`
second. No committed custom package-host configuration, local `mavenLocal()`
fallback, or extra credentials are required for the shared gates.

Renderer output artifacts from `./gradlew renderAgents` are written under `build/rendered/`:

- Codex: `build/rendered/codex/.codex/agents/*.toml`
- Claude: `build/rendered/claude/.claude/agents/*.md`
- Copilot: `build/rendered/copilot/.github/agents/*.agent.md`

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
