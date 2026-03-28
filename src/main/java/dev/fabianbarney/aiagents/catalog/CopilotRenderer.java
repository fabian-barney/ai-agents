package dev.fabianbarney.aiagents.catalog;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
final class CopilotRenderer extends BaseRenderer {

    @Override
    protected String compatibleProvider() {
        return "github";
    }

    @Override
    protected String defaultModel() {
        return "gpt-5";
    }

    @Override
    protected Path relativePath(AgentDefinition agent) {
        return Path.of("copilot", ".github", "agents", "%s.agent.md".formatted(agent.id()));
    }

    @Override
    protected String renderContent(AgentDefinition agent) {
        CopilotOverrides overrides = agent.platformOverrides().copilot();
        StringBuilder builder = new StringBuilder();
        builder.append("---").append(System.lineSeparator());
        builder.append("name: ").append(yamlQuoted(agent.name())).append(System.lineSeparator());
        builder.append("description: ").append(yamlQuoted(description(agent, overrides))).append(System.lineSeparator());
        builder.append("model: ").append(yamlQuoted(selectedModel(agent, overrides.model()))).append(System.lineSeparator());
        if (overrides.target() != null && !overrides.target().isBlank()) {
            builder.append("target: ").append(yamlQuoted(overrides.target())).append(System.lineSeparator());
        }
        renderStringList(builder, "tools", overrides.tools());
        renderStringList(builder, "mcp-servers", overrides.mcpServers());
        builder.append("---").append(System.lineSeparator()).append(System.lineSeparator());
        builder.append(markdownBody(agent.prompt()));
        return builder.toString();
    }

    private String description(AgentDefinition agent, CopilotOverrides overrides) {
        return overrides.description() == null || overrides.description().isBlank()
            ? agent.purpose()
            : overrides.description();
    }

    private void renderStringList(StringBuilder builder, String key, List<String> values) {
        if (values.isEmpty()) {
            return;
        }
        builder.append(key).append(':').append(System.lineSeparator());
        for (String value : values) {
            builder.append("  - ").append(yamlQuoted(value)).append(System.lineSeparator());
        }
    }
}
