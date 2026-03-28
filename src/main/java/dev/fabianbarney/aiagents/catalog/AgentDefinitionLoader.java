package dev.fabianbarney.aiagents.catalog;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public final class AgentDefinitionLoader {

    private final ObjectMapper mapper;

    public AgentDefinitionLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    public List<AgentDefinition> load(Path inputDirectory) throws IOException {
        if (!Files.isDirectory(inputDirectory)) {
            throw new IllegalArgumentException(
                "Canonical agent directory does not exist: %s".formatted(inputDirectory)
            );
        }

        List<Path> agentFiles;
        try (var paths = Files.list(inputDirectory)) {
            agentFiles = paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".yaml") || path.toString().endsWith(".yml"))
                .sorted(Comparator.naturalOrder())
                .toList();
        }

        if (agentFiles.isEmpty()) {
            throw new IllegalArgumentException(
                "No canonical agent definitions were found under %s".formatted(inputDirectory)
            );
        }

        List<AgentDefinition> agents = new ArrayList<>();
        Map<String, Path> seenIds = new HashMap<>();
        for (Path agentFile : agentFiles) {
            JsonNode rootNode = mapper.readTree(agentFile.toFile());
            validateTree(agentFile, rootNode);
            AgentDefinition definition = mapper.treeToValue(rootNode, AgentDefinition.class);
            validateDefinition(agentFile, definition, seenIds);
            agents.add(definition);
        }
        return agents;
    }

    private void validateTree(Path agentFile, JsonNode rootNode) {
        requireNonEmptyArray(agentFile, rootNode, "whenToUse");
        requireNonEmptyArray(agentFile, rootNode, "boundaries");
        requireOptionalNonEmptyArray(agentFile, rootNode, "preferredModels");
    }

    private void requireNonEmptyArray(Path agentFile, JsonNode rootNode, String fieldName) {
        JsonNode node = rootNode.get(fieldName);
        if (node == null || !node.isArray() || node.isEmpty()) {
            throw validationError(agentFile, "%s must be a non-empty list".formatted(fieldName));
        }
    }

    private void requireOptionalNonEmptyArray(Path agentFile, JsonNode rootNode, String fieldName) {
        JsonNode node = rootNode.get(fieldName);
        if (node != null && (!node.isArray() || node.isEmpty())) {
            throw validationError(agentFile, "%s must be omitted or contain at least one entry".formatted(fieldName));
        }
    }

    private void validateDefinition(Path agentFile, AgentDefinition definition, Map<String, Path> seenIds) {
        requireNonBlank(agentFile, "id", definition.id());
        requireNonBlank(agentFile, "name", definition.name());
        requireNonBlank(agentFile, "purpose", definition.purpose());
        requireNonBlank(agentFile, "prompt", definition.prompt());
        requireNonBlankValues(agentFile, "whenToUse", definition.whenToUse());
        requireNonBlankValues(agentFile, "boundaries", definition.boundaries());
        requireNonBlankValues(agentFile, "aliases", definition.aliases());
        requireNonBlankValues(agentFile, "examples", definition.examples());
        requireNonBlankValues(agentFile, "toolHints", definition.toolHints());
        requireNonBlankValues(agentFile, "notes", definition.notes());

        Path previousPath = seenIds.putIfAbsent(definition.id(), agentFile);
        if (previousPath != null) {
            throw validationError(
                agentFile,
                "Duplicate agent id '%s' already defined in %s".formatted(definition.id(), previousPath)
            );
        }

        for (PreferredModel preferredModel : definition.preferredModels()) {
            requireNonBlank(agentFile, "preferredModels.provider", preferredModel.provider());
            requireNonBlank(agentFile, "preferredModels.model", preferredModel.model());
        }
    }

    private void requireNonBlank(Path agentFile, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw validationError(agentFile, "%s must be non-blank".formatted(fieldName));
        }
    }

    private void requireNonBlankValues(Path agentFile, String fieldName, List<String> values) {
        for (String value : values) {
            if (value == null || value.isBlank()) {
                throw validationError(agentFile, "%s must not contain blank entries".formatted(fieldName));
            }
        }
    }

    private IllegalArgumentException validationError(Path agentFile, String message) {
        return new IllegalArgumentException("%s: %s".formatted(agentFile, message));
    }
}
