package dev.fabianbarney.aiagents.catalog;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Service
public final class AgentCatalogService {

    private final AgentDefinitionLoader loader;
    private final List<Renderer> renderers;

    public AgentCatalogService(AgentDefinitionLoader loader, List<Renderer> renderers) {
        this.loader = loader;
        this.renderers = List.copyOf(renderers);
    }

    public void renderCatalog(Path inputDirectory, Path outputDirectory) throws IOException {
        List<AgentDefinition> agents = loader.load(inputDirectory);
        prepareOutputDirectory(outputDirectory);

        for (Renderer renderer : renderers) {
            renderer.renderAll(agents, outputDirectory);
        }
    }

    private void prepareOutputDirectory(Path outputDirectory) throws IOException {
        if (Files.exists(outputDirectory)) {
            try (var paths = Files.walk(outputDirectory)) {
                paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(outputDirectory))
                    .forEach(this::deletePath);
            }
        }

        Files.createDirectories(outputDirectory);
    }

    private void deletePath(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Failed to clean generated output path %s".formatted(path),
                exception
            );
        }
    }
}
