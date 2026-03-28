package dev.fabianbarney.aiagents.catalog;

import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
final class CodexRenderer extends BaseRenderer {

    @Override
    protected String compatibleProvider() {
        return "openai";
    }

    @Override
    protected String defaultModel() {
        return "gpt-5.4";
    }

    @Override
    protected Path relativePath(AgentDefinition agent) {
        return Path.of("codex", ".codex", "agents", "%s.toml".formatted(agent.id()));
    }

    @Override
    protected String renderContent(AgentDefinition agent) {
        CodexOverrides overrides = agent.platformOverrides().codex();
        StringBuilder builder = new StringBuilder();
        builder.append("name = ").append(quoted(agent.name())).append(System.lineSeparator());
        builder.append("description = ").append(quoted(description(agent, overrides))).append(System.lineSeparator());
        builder.append("model = ").append(quoted(selectedModel(agent, overrides.model()))).append(System.lineSeparator());

        String reasoningEffort = selectedReasoningEffort(agent, overrides.modelReasoningEffort());
        if (reasoningEffort != null) {
            builder.append("model_reasoning_effort = ")
                .append(quoted(reasoningEffort))
                .append(System.lineSeparator());
        }
        if (overrides.sandboxMode() != null && !overrides.sandboxMode().isBlank()) {
            builder.append("sandbox_mode = ").append(quoted(overrides.sandboxMode())).append(System.lineSeparator());
        }
        if (!overrides.mcpServers().isEmpty()) {
            builder.append("mcp_servers = [")
                .append(renderTomlArray(overrides.mcpServers()))
                .append(']')
                .append(System.lineSeparator());
        }
        if (!overrides.nicknameCandidates().isEmpty()) {
            builder.append("nickname_candidates = [")
                .append(renderTomlArray(overrides.nicknameCandidates()))
                .append(']')
                .append(System.lineSeparator());
        }

        builder.append("developer_instructions = ")
            .append(tomlLiteralBlock(agent.prompt()))
            .append(System.lineSeparator());
        return builder.toString();
    }

    private String description(AgentDefinition agent, CodexOverrides overrides) {
        return overrides.description() == null || overrides.description().isBlank()
            ? agent.purpose()
            : overrides.description();
    }

    private String renderTomlArray(java.util.List<String> values) {
        return values.stream().map(this::quoted).collect(java.util.stream.Collectors.joining(", "));
    }
}
