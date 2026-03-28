package dev.fabianbarney.aiagents.catalog;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public final class AgentDefinitionLoader {

    private final Validator validator;
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

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
        requireOptionalNonEmptyArray(agentFile, rootNode, "preferredModels");
    }

    private void requireOptionalNonEmptyArray(Path agentFile, JsonNode rootNode, String fieldName) {
        JsonNode node = rootNode.get(fieldName);
        if (node != null && (!node.isArray() || node.isEmpty())) {
            throw validationError(agentFile, "%s must be omitted or contain at least one entry".formatted(fieldName));
        }
    }

    private void validateDefinition(Path agentFile, AgentDefinition definition, Map<String, Path> seenIds) {
        validateBean(agentFile, definition);

        Path previousPath = seenIds.putIfAbsent(definition.id(), agentFile);
        if (previousPath != null) {
            throw validationError(
                agentFile,
                "Duplicate agent id '%s' already defined in %s".formatted(definition.id(), previousPath)
            );
        }
    }

    private IllegalArgumentException validationError(Path agentFile, String message) {
        return new IllegalArgumentException("%s: %s".formatted(agentFile, message));
    }

    private void validateBean(Path agentFile, AgentDefinition definition) {
        List<String> violations = validator.validate(definition).stream()
            .map(this::formatViolation)
            .sorted()
            .toList();

        if (!violations.isEmpty()) {
            throw validationError(agentFile, String.join("; ", violations));
        }
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString()
            .replaceAll("\\[[0-9]+\\]", "")
            .replace(".<list element>", "")
            .replace(".value", "");

        return path.isBlank()
            ? violation.getMessage()
            : "%s %s".formatted(path, violation.getMessage());
    }
}
