package dev.fabianbarney.aiagents.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public final class AgentCatalogService {

    private final AgentDefinitionLoader loader;
    private final List<Renderer> renderers;

    public void renderCatalog(Path inputDirectory, Path outputDirectory) throws IOException {
        List<AgentDefinition> agents = loader.load(inputDirectory);
        Path normalizedOutputDirectory = outputDirectory.toAbsolutePath().normalize();
        prepareOutputDirectory(normalizedOutputDirectory);

        for (Renderer renderer : List.copyOf(renderers)) {
            renderer.renderAll(agents, normalizedOutputDirectory);
        }
    }

    private void prepareOutputDirectory(Path outputDirectory) throws IOException {
        validateOutputDirectory(outputDirectory);

        if (Files.exists(outputDirectory)) {
            try (var paths = Files.walk(outputDirectory)) {
                paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(outputDirectory))
                    .forEach(this::deletePath);
            }
        }

        Files.createDirectories(outputDirectory);
    }

    private void validateOutputDirectory(Path outputDirectory) {
        Path projectDirectory = projectDirectory();
        Path allowedOutputRoot = allowedOutputRoot();
        Path root = outputDirectory.getRoot();

        if (root != null && outputDirectory.equals(root)) {
            throw new IllegalArgumentException(
                "Refusing to use filesystem root as catalog output directory: %s".formatted(outputDirectory)
            );
        }

        if (outputDirectory.equals(projectDirectory)) {
            throw new IllegalArgumentException(
                "Refusing to use project directory as catalog output directory: %s".formatted(outputDirectory)
            );
        }

        if (!outputDirectory.startsWith(allowedOutputRoot)) {
            throw new IllegalArgumentException(
                "Catalog output directory must be located under %s but was %s"
                    .formatted(allowedOutputRoot, outputDirectory)
            );
        }
    }

    private Path allowedOutputRoot() {
        return projectDirectory().resolve(Path.of("build", "rendered")).normalize();
    }

    private Path projectDirectory() {
        return Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
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
