package media.barney.ai.agent.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
final class CopilotRenderer extends BaseRenderer {

    private final RendererProperties rendererProperties;

    @Override
    protected ProviderId compatibleProvider() {
        return rendererProperties.getCopilot().getCompatibleProvider();
    }

    @Override
    protected ModelId defaultModel() {
        return rendererProperties.getCopilot().getDefaultModel();
    }

    @Override
    protected Path relativePath(AgentDefinition agent) {
        RendererProperties.Copilot properties = rendererProperties.getCopilot();
        return properties.getOutputDirectory().resolve("%s%s".formatted(agent.id(), properties.getFileSuffix()));
    }

    @Override
    protected String renderContent(AgentDefinition agent) {
        RendererProperties.Copilot properties = rendererProperties.getCopilot();
        CopilotOverrides overrides = agent.platformOverrides().copilot();
        StringBuilder builder = new StringBuilder();
        builder.append(properties.getFrontmatterDelimiter()).append(System.lineSeparator());
        builder.append(properties.getNameKey()).append(": ").append(yamlQuoted(agent.name())).append(System.lineSeparator());
        builder.append(properties.getDescriptionKey()).append(": ")
            .append(yamlQuoted(description(agent, overrides)))
            .append(System.lineSeparator());
        builder.append(properties.getModelKey()).append(": ")
            .append(yamlQuoted(selectedModel(agent, overrides.model()).value()))
            .append(System.lineSeparator());
        if (overrides.target() != null && !overrides.target().isBlank()) {
            builder.append(properties.getTargetKey()).append(": ")
                .append(yamlQuoted(overrides.target()))
                .append(System.lineSeparator());
        }
        renderStringList(builder, properties.getToolsKey(), overrides.tools());
        renderStringList(builder, properties.getMcpServersKey(), overrides.mcpServers());
        builder.append(properties.getFrontmatterDelimiter()).append(System.lineSeparator()).append(System.lineSeparator());
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
