# ai-agents Specification

## Purpose

`ai-agents` is the canonical repository for tool-agnostic AI agents.

An agent is a named worker persona with a clear purpose, activation criteria,
behavioral boundaries, and built-in prompt/instructions. This repository
exists to:

- define a canonical, portable agent format
- keep agent authoring rules explicit and versioned
- render the same agent definition into Codex, Claude, and Copilot artifacts
- keep target-specific differences narrow, deterministic, and configurable

## Relationship to `ai-skills`

`ai-agents` and `ai-skills` solve related but different problems.

- `ai-agents` is a catalog of tool-agnostic agents plus the JVM validation and
  renderer pipeline that emits target-specific agent artifacts.
- `ai-skills` is a catalog of reusable skill bundles and supporting assets.

The main difference is the unit of reuse:

- an agent is a named worker with responsibility boundaries and a built-in
  prompt
- a skill is a reusable capability or playbook that a host environment may
  load at runtime

In v1, project-local instruction files, installed skills, tool integrations,
and other runtime context remain outside the central catalog. The canonical
agent definition must stay self-contained, and host environments resolve extra
runtime context themselves.

## Canonical Agent Unit

The canonical unit is one YAML document per agent under:

```text
agents/
```

The current seed catalog contains these canonical agents:

- `orchestrator`
- `explorer`
- `implementer`
- `reviewer`

Repository and loader rules in v1:

- the loader reads regular `.yaml` and `.yml` files from the top level of
  `agents/`
- agent files are processed in sorted path order for stable loading
- each agent must have a unique `id`
- the repository currently keeps one committed file per agent

## Canonical Agent Schema

Each canonical agent definition is a single YAML document.

### Required Fields

Every agent must define:

- `id`
- `name`
- `purpose`
- `whenToUse`
- `boundaries`
- `prompt`

`id`, `name`, `purpose`, and `prompt` must be non-blank strings.

`whenToUse` and `boundaries` must be non-empty lists of non-blank strings.

### Optional Fields

The canonical schema also supports:

- `aliases`
- `examples`
- `toolHints`
- `notes`
- `preferredModels`
- `platformOverrides`

Optional list-valued fields may be omitted or empty, except `preferredModels`.
If `preferredModels` is present, it must contain at least one entry.

Each `preferredModels` entry uses:

```yaml
- provider: openai
  model: gpt-5.4
  reasoningEffort: high
```

`preferredModels` is ordered from most preferred to least preferred. A
renderer scans the list from top to bottom and uses the first entry whose
`provider` matches that renderer's compatible provider. If no compatible entry
exists, the renderer falls back to its target-specific default model behavior.

The loader rejects unknown top-level properties and duplicate agent ids.

### `platformOverrides`

`platformOverrides` is the only canonical place for narrow target-specific
differences. v1 branches are:

- `codex`
- `claude`
- `copilot`

Current override fields are:

- `codex`: `description`, `model`, `modelReasoningEffort`, `sandboxMode`,
  `mcpServers`, `nicknameCandidates`
- `claude`: `description`, `tools`
- `copilot`: `description`, `model`, `tools`, `mcpServers`, `target`

Overrides are metadata-only refinements. They may narrow descriptions, tool
lists, target fields, or execution hints, but they do not replace the
canonical `prompt`.

## Authoring Rules

Canonical agents in this repository must follow these rules:

- keep each agent focused on one worker role or responsibility slice
- embed the full behavioral contract in the agent definition itself
- keep prompts self-contained instead of depending on a shared instruction
  layer
- use `platformOverrides` only for true renderer-specific differences
- prefer narrow explicit specialists over broad multi-role prompts
- record breaking schema or override semantics changes through ADRs

This repository does not centrally model capabilities, environments, skills,
shared instruction fragments, shared output contracts, or orchestration
envelopes in v1.

The `orchestrator` agent is intentionally thin. Coordination, delegation, and
synthesis belong there; specialist behavior belongs in specialist agents.

## Renderer and Target Mapping

The renderer pipeline is a non-web Spring Boot application.
`AgentCatalogApplication` boots the app with `WebApplicationType.NONE`, and
`CatalogRunner` invokes the catalog service on startup.

Default execution paths:

- `./gradlew renderAgents`
- `--catalog.input=<path>`
- `--catalog.output=<path>`

Default catalog properties:

- `catalog.input=agents`
- `catalog.output=build/rendered`

Renderers load canonical definitions, validate them, and write generated
artifacts under the configured output root. Common behavior in v1:

- renderer output subpaths must remain relative to the prepared output root
- per-target output directories and emitted field names are configurable
  through `catalog.renderers.*`
- generated artifacts embed the canonical prompt directly
- model selection uses a per-target override first, then the first compatible
  `preferredModels` entry, then the renderer default if the target emits a
  model

### Codex

Codex output is written under:

```text
build/rendered/codex/.codex/agents/*.toml
```

Current Codex defaults are:

- compatible provider: `openai`
- default model: `gpt-5.4`
- default output directory: `codex/.codex/agents`
- default file suffix: `.toml`

The Codex renderer emits TOML and maps:

- `name` from the canonical agent `name`
- `description` from the canonical `purpose` unless overridden
- `model` from `platformOverrides.codex.model`, otherwise model selection
  rules
- `model_reasoning_effort` from
  `platformOverrides.codex.modelReasoningEffort` or the selected preferred
  model entry when present
- `sandbox_mode`, `mcp_servers`, and `nickname_candidates` from Codex
  overrides
- `developer_instructions` from the canonical `prompt`

Current configurable Codex properties are:

- `catalog.renderers.codex.compatible-provider`
- `catalog.renderers.codex.default-model`
- `catalog.renderers.codex.output-directory`
- `catalog.renderers.codex.file-suffix`
- `catalog.renderers.codex.name-key`
- `catalog.renderers.codex.description-key`
- `catalog.renderers.codex.model-key`
- `catalog.renderers.codex.model-reasoning-effort-key`
- `catalog.renderers.codex.sandbox-mode-key`
- `catalog.renderers.codex.mcp-servers-key`
- `catalog.renderers.codex.nickname-candidates-key`
- `catalog.renderers.codex.developer-instructions-key`

### Claude

Claude output is written under:

```text
build/rendered/claude/.claude/agents/*.md
```

Current Claude defaults are:

- compatible provider: `anthropic`
- default output directory: `claude/.claude/agents`
- default file suffix: `.md`
- default frontmatter delimiter: `---`

The Claude renderer emits Markdown with YAML frontmatter and maps:

- `name` from the canonical agent `id`
- `description` from the canonical `purpose` unless overridden
- `tools` from `platformOverrides.claude.tools`
- the Markdown body from the canonical `prompt`

Claude renderer configuration also includes `default-model` and
`compatible-provider`, even though the current Claude artifact shape does not
emit a model field.

Current configurable Claude properties are:

- `catalog.renderers.claude.compatible-provider`
- `catalog.renderers.claude.default-model`
- `catalog.renderers.claude.output-directory`
- `catalog.renderers.claude.file-suffix`
- `catalog.renderers.claude.frontmatter-delimiter`
- `catalog.renderers.claude.name-key`
- `catalog.renderers.claude.description-key`
- `catalog.renderers.claude.tools-key`

### Copilot

Copilot output is written under:

```text
build/rendered/copilot/.github/agents/*.agent.md
```

Current Copilot defaults are:

- compatible provider: `github`
- default model: `gpt-5`
- default output directory: `copilot/.github/agents`
- default file suffix: `.agent.md`
- default frontmatter delimiter: `---`

The Copilot renderer emits Markdown with YAML frontmatter and maps:

- `name` from the canonical agent `name`
- `description` from the canonical `purpose` unless overridden
- `model` from `platformOverrides.copilot.model`, otherwise model selection
  rules
- `target` from `platformOverrides.copilot.target` when present
- `tools` and `mcp-servers` from Copilot overrides
- the Markdown body from the canonical `prompt`

Current configurable Copilot properties are:

- `catalog.renderers.copilot.compatible-provider`
- `catalog.renderers.copilot.default-model`
- `catalog.renderers.copilot.output-directory`
- `catalog.renderers.copilot.file-suffix`
- `catalog.renderers.copilot.frontmatter-delimiter`
- `catalog.renderers.copilot.name-key`
- `catalog.renderers.copilot.description-key`
- `catalog.renderers.copilot.model-key`
- `catalog.renderers.copilot.target-key`
- `catalog.renderers.copilot.tools-key`
- `catalog.renderers.copilot.mcp-servers-key`

## Output Safety and Determinism

The renderer pipeline is intentionally strict about where it writes output and
how generated content is encoded.

v1 guarantees and safeguards are:

- rendering prepares a clean output directory before writing generated artifacts
- the configured catalog output directory must stay under `build/rendered`
- the renderer refuses to use the project root or a filesystem root as the
  catalog output directory
- existing path chains are validated to prevent symlink traversal outside the
  allowed output root
- renderer output paths must be relative and must stay under the prepared
  output root after normalization
- repeated renders of the same canonical inputs produce deterministic output
- Codex developer instructions are emitted as TOML basic strings with escaping
  for control characters and embedded quotes

## Repository Conventions

The repository-level conventions are:

- `agents/` is reserved for canonical YAML agent definitions
- `src/main/java/` contains the renderer and validation implementation
- `ai/PROJECT/DECISIONS/` contains ADRs for schema and architecture changes
- `build/rendered/` is reserved for generated artifacts
- rendered artifacts are build outputs only and are not committed to git
- `spec.md` is the repository contract until later ADRs or implementation
  changes supersede parts of it

## Example Agent Definition

The following example is illustrative. It shows the canonical shape and the
intended use of ordered model preferences plus narrow platform overrides.

```yaml
id: example-investigator
name: Example Investigator
purpose: Investigate a bounded code or configuration question and summarize the evidence.
whenToUse:
  - codebase exploration
  - read-heavy investigation
boundaries:
  - stay read-heavy
  - gather evidence before concluding
  - avoid speculative implementation changes
prompt: |
  You are a focused exploration agent.
  Inspect the available code and configuration, then return the smallest
  useful evidence-backed summary for the parent agent.
preferredModels:
  - provider: openai
    model: gpt-5.4
    reasoningEffort: medium
  - provider: github
    model: gpt-5
  - provider: anthropic
    model: claude-sonnet-4-5
platformOverrides:
  codex:
    sandboxMode: read-only
  claude:
    tools:
      - Read
      - Grep
  copilot:
    tools:
      - codebase
```

## Near-Term Direction

Near-term evolution should stay conservative and ADR-driven.

Likely extension points are:

- adding more canonical agents without introducing a shared capability registry
- adding more renderers when a host environment needs a distinct artifact
  format
- expanding the schema additively when repeated authoring or rendering pain
  justifies a new ADR
- continuing to keep runtime skills, project-local instructions, and other
  host context outside the central catalog

## Non-Goals for v1

v1 does not include:

- shared instruction fragments or a central prompt-composition layer
- a central capability, environment, or skill model
- committed rendered output trees
- runtime orchestration envelopes beyond self-contained agent definitions
- release artifact publishing or packaging beyond the current renderer outputs
- stronger roadmap commitments than additive, ADR-driven catalog and renderer
  growth
