package dev.fabianbarney.aiagents.catalog;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
final class ClaudeRenderer extends BaseRenderer {

    @Override
    protected String compatibleProvider() {
        return "anthropic";
    }

    @Override
    protected String defaultModel() {
        return "claude-sonnet-4-5";
    }

    @Override
    protected Path relativePath(AgentDefinition agent) {
        return Path.of("claude", ".claude", "agents", "%s.md".formatted(agent.id()));
    }

    @Override
    protected String renderContent(AgentDefinition agent) {
        ClaudeOverrides overrides = agent.platformOverrides().claude();
        StringBuilder builder = new StringBuilder();
        builder.append("---").append(System.lineSeparator());
        builder.append("name: ").append(yamlQuoted(agent.id())).append(System.lineSeparator());
        builder.append("description: ").append(yamlQuoted(description(agent, overrides))).append(System.lineSeparator());
        renderStringList(builder, "tools", overrides.tools());
        builder.append("---").append(System.lineSeparator()).append(System.lineSeparator());
        builder.append(markdownBody(agent.prompt()));
        return builder.toString();
    }

    private String description(AgentDefinition agent, ClaudeOverrides overrides) {
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
