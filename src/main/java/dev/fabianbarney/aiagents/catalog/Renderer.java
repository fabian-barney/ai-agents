package dev.fabianbarney.aiagents.catalog;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
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
        Path normalizedOutputRoot = outputRoot.toAbsolutePath().normalize();
        for (AgentDefinition agent : agents) {
            Path outputPath = resolveOutputPath(normalizedOutputRoot, relativePath(agent));
            writeFile(outputPath, renderContent(agent));
        }
    }

    protected final ModelId selectedModel(AgentDefinition agent, @Nullable ModelId overrideModel) {
        if (overrideModel != null && !overrideModel.isBlank()) {
            return overrideModel;
        }

        return selectedPreferredModel(agent)
            .map(PreferredModel::model)
            .orElse(defaultModel());
    }

    protected final @Nullable String selectedReasoningEffort(
        AgentDefinition agent,
        @Nullable String overrideReasoningEffort
    ) {
        if (overrideReasoningEffort != null && !overrideReasoningEffort.isBlank()) {
            return overrideReasoningEffort;
        }

        PreferredModel preferredModel = selectedPreferredModel(agent).orElse(null);
        if (preferredModel == null) {
            return null;
        }

        @Nullable String reasoningEffort = preferredModel.reasoningEffort();
        return reasoningEffort == null || reasoningEffort.isBlank() ? null : reasoningEffort;
    }

    private Optional<PreferredModel> selectedPreferredModel(AgentDefinition agent) {
        return agent.preferredModels().stream()
            .filter(model -> compatibleProvider().equals(model.provider()))
            .findFirst();
    }

    private Path resolveOutputPath(Path outputRoot, Path rendererRelativePath) {
        if (rendererRelativePath.isAbsolute()) {
            throw new IllegalArgumentException(
                "Renderer output path must be relative to %s but was %s"
                    .formatted(outputRoot, rendererRelativePath)
            );
        }

        Path resolvedOutputPath = outputRoot.resolve(rendererRelativePath).normalize();
        if (!resolvedOutputPath.startsWith(outputRoot)) {
            throw new IllegalArgumentException(
                "Renderer output path must stay under %s but was %s"
                    .formatted(outputRoot, rendererRelativePath)
            );
        }

        return resolvedOutputPath;
    }

    protected final void writeFile(Path outputPath, String content) throws IOException {
        Path parentDirectory = outputPath.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }
        Files.writeString(outputPath, content);
    }

    protected final String quoted(String value) {
        return "\"%s\"".formatted(value.replace("\\", "\\\\").replace("\"", "\\\""));
    }

    protected final String yamlQuoted(String value) {
        return quoted(value);
    }

    protected final String tomlBasicString(String value) {
        StringBuilder builder = new StringBuilder("\"");
        for (char character : value.stripTrailing().toCharArray()) {
            switch (character) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\b' -> builder.append("\\b");
                case '\t' -> builder.append("\\t");
                case '\n' -> builder.append("\\n");
                case '\f' -> builder.append("\\f");
                case '\r' -> builder.append("\\r");
                default -> {
                    if (Character.isISOControl(character)) {
                        builder.append("\\u%04X".formatted((int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        builder.append('"');
        return builder.toString();
    }

    protected final String markdownBody(String prompt) {
        return prompt.stripTrailing() + System.lineSeparator();
    }
}
