package dev.fabianbarney.aiagents.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
final class CodexRenderer extends BaseRenderer {

    private final RendererProperties rendererProperties;

    @Override
    protected ProviderId compatibleProvider() {
        return rendererProperties.getCodex().getCompatibleProvider();
    }

    @Override
    protected ModelId defaultModel() {
        return rendererProperties.getCodex().getDefaultModel();
    }

    @Override
    protected Path relativePath(AgentDefinition agent) {
        RendererProperties.Codex properties = rendererProperties.getCodex();
        return properties.getOutputDirectory().resolve("%s%s".formatted(agent.id(), properties.getFileSuffix()));
    }

    @Override
    protected String renderContent(AgentDefinition agent) {
        RendererProperties.Codex properties = rendererProperties.getCodex();
        CodexOverrides overrides = agent.platformOverrides().codex();
        StringBuilder builder = new StringBuilder();
        builder.append(properties.getNameKey()).append(" = ").append(tomlBasicString(agent.name())).append(System.lineSeparator());
        builder.append(properties.getDescriptionKey()).append(" = ")
            .append(tomlBasicString(description(agent, overrides)))
            .append(System.lineSeparator());
        builder.append(properties.getModelKey()).append(" = ")
            .append(tomlBasicString(selectedModel(agent, overrides.model()).value()))
            .append(System.lineSeparator());

        String reasoningEffort = selectedReasoningEffort(agent, overrides.modelReasoningEffort());
        if (reasoningEffort != null) {
            builder.append(properties.getModelReasoningEffortKey()).append(" = ")
                .append(tomlBasicString(reasoningEffort))
                .append(System.lineSeparator());
        }
        if (overrides.sandboxMode() != null && !overrides.sandboxMode().isBlank()) {
            builder.append(properties.getSandboxModeKey()).append(" = ")
                .append(tomlBasicString(overrides.sandboxMode()))
                .append(System.lineSeparator());
        }
        if (!overrides.mcpServers().isEmpty()) {
            builder.append(properties.getMcpServersKey()).append(" = [")
                .append(renderTomlArray(overrides.mcpServers()))
                .append(']')
                .append(System.lineSeparator());
        }
        if (!overrides.nicknameCandidates().isEmpty()) {
            builder.append(properties.getNicknameCandidatesKey()).append(" = [")
                .append(renderTomlArray(overrides.nicknameCandidates()))
                .append(']')
                .append(System.lineSeparator());
        }

        builder.append(properties.getDeveloperInstructionsKey()).append(" = ")
            .append(tomlBasicString(agent.prompt()))
            .append(System.lineSeparator());
        return builder.toString();
    }

    private String description(AgentDefinition agent, CodexOverrides overrides) {
        return overrides.description() == null || overrides.description().isBlank()
            ? agent.purpose()
            : overrides.description();
    }

    private String renderTomlArray(java.util.List<String> values) {
        return values.stream().map(this::tomlBasicString).collect(java.util.stream.Collectors.joining(", "));
    }
}
