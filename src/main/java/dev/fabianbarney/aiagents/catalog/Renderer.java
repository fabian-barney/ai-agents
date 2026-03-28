package dev.fabianbarney.aiagents.catalog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

interface Renderer {
    void renderAll(List<AgentDefinition> agents, Path outputRoot) throws IOException;
}

abstract class BaseRenderer implements Renderer {

    protected abstract ProviderId compatibleProvider();

    protected abstract ModelId defaultModel();

    protected abstract Path relativePath(AgentDefinition agent);

    protected abstract String renderContent(AgentDefinition agent);

    @Override
    public final void renderAll(List<AgentDefinition> agents, Path outputRoot) throws IOException {
        for (AgentDefinition agent : agents) {
            Path outputPath = outputRoot.resolve(relativePath(agent));
            writeFile(outputPath, renderContent(agent));
        }
    }

    protected final ModelId selectedModel(AgentDefinition agent, ModelId overrideModel) {
        if (overrideModel != null && !overrideModel.isBlank()) {
            return overrideModel;
        }

        return selectedPreferredModel(agent)
            .map(PreferredModel::model)
            .orElse(defaultModel());
    }

    protected final String selectedReasoningEffort(AgentDefinition agent, String overrideReasoningEffort) {
        if (overrideReasoningEffort != null && !overrideReasoningEffort.isBlank()) {
            return overrideReasoningEffort;
        }

        return selectedPreferredModel(agent)
            .map(PreferredModel::reasoningEffort)
            .filter(value -> value != null && !value.isBlank())
            .orElse(null);
    }

    private Optional<PreferredModel> selectedPreferredModel(AgentDefinition agent) {
        return agent.preferredModels().stream()
            .filter(model -> compatibleProvider().equals(model.provider()))
            .findFirst();
    }

    protected final void writeFile(Path outputPath, String content) throws IOException {
        Path parentDirectory = outputPath.getParent();
        if (parentDirectory != null) {
            java.nio.file.Files.createDirectories(parentDirectory);
        }
        java.nio.file.Files.writeString(outputPath, content);
    }

    protected final String quoted(String value) {
        return "\"%s\"".formatted(value.replace("\\", "\\\\").replace("\"", "\\\""));
    }

    protected final String yamlQuoted(String value) {
        return quoted(value);
    }

    protected final String tomlLiteralBlock(String value) {
        return "'''\n%s\n'''".formatted(value.stripTrailing());
    }

    protected final String markdownBody(String prompt) {
        return prompt.stripTrailing() + System.lineSeparator();
    }
}
