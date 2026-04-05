package media.barney.ai.agent.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
final class ClaudeRenderer extends BaseRenderer {

    private final RendererProperties rendererProperties;

    @Override
    protected ProviderId compatibleProvider() {
        return rendererProperties.getClaude().getCompatibleProvider();
    }

    @Override
    protected ModelId defaultModel() {
        return rendererProperties.getClaude().getDefaultModel();
    }

    @Override
    protected Path relativePath(AgentDefinition agent) {
        RendererProperties.Claude properties = rendererProperties.getClaude();
        return properties.getOutputDirectory().resolve("%s%s".formatted(agent.id(), properties.getFileSuffix()));
    }

    @Override
    protected String renderContent(AgentDefinition agent) {
        RendererProperties.Claude properties = rendererProperties.getClaude();
        ClaudeOverrides overrides = agent.platformOverrides().claude();
        StringBuilder builder = new StringBuilder();
        builder.append(properties.getFrontmatterDelimiter()).append(System.lineSeparator());
        builder.append(properties.getNameKey()).append(": ").append(yamlQuoted(agent.id())).append(System.lineSeparator());
        builder.append(properties.getDescriptionKey()).append(": ")
            .append(yamlQuoted(description(agent, overrides)))
            .append(System.lineSeparator());
        renderStringList(builder, properties.getToolsKey(), overrides.tools());
        builder.append(properties.getFrontmatterDelimiter()).append(System.lineSeparator()).append(System.lineSeparator());
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
