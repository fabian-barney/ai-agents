package dev.fabianbarney.aiagents.catalog;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RendererIntegrationTest {

    private final RendererProperties rendererProperties = new RendererProperties();
    private final AgentCatalogService service = new AgentCatalogService(
        new AgentDefinitionLoader(Validation.buildDefaultValidatorFactory().getValidator()),
        List.of(
            new CodexRenderer(rendererProperties),
            new ClaudeRenderer(rendererProperties),
            new CopilotRenderer(rendererProperties)
        )
    );

    @Test
    void rendersAllExpectedFilesIntoTargetSpecificDirectories() throws IOException {
        Path outputDirectory = generatedOutputDirectory();
        service.renderCatalog(projectPath("agents"), outputDirectory);

        for (String agentId : List.of("orchestrator", "explorer", "implementer", "reviewer")) {
            assertTrue(Files.exists(outputDirectory.resolve(Path.of("codex", ".codex", "agents", agentId + ".toml"))));
            assertTrue(Files.exists(outputDirectory.resolve(Path.of("claude", ".claude", "agents", agentId + ".md"))));
            assertTrue(Files.exists(outputDirectory.resolve(Path.of("copilot", ".github", "agents", agentId + ".agent.md"))));
        }
    }

    @Test
    void codexOutputIncludesModelSelectionAndSandboxOverrides() throws IOException {
        Path outputDirectory = generatedOutputDirectory();
        service.renderCatalog(projectPath("agents"), outputDirectory);

        String rendered = Files.readString(
            outputDirectory.resolve(Path.of("codex", ".codex", "agents", "implementer.toml"))
        );

        assertTrue(rendered.contains("name = \"Implementer\""));
        assertTrue(rendered.contains("model = \"gpt-5.4\""));
        assertTrue(rendered.contains("model_reasoning_effort = \"medium\""));
        assertTrue(rendered.contains("sandbox_mode = \"workspace-write\""));
        assertTrue(rendered.contains("developer_instructions = '''"));
    }

    @Test
    void claudeOutputIncludesFrontmatterAndPromptBody() throws IOException {
        Path outputDirectory = generatedOutputDirectory();
        service.renderCatalog(projectPath("agents"), outputDirectory);

        String rendered = Files.readString(
            outputDirectory.resolve(Path.of("claude", ".claude", "agents", "explorer.md"))
        );

        assertTrue(rendered.startsWith("---"));
        assertTrue(rendered.contains("name: \"explorer\""));
        assertTrue(rendered.contains("description: \"Investigate code, configuration, and context"));
        assertTrue(rendered.contains("tools:"));
        assertTrue(rendered.contains("You are a focused exploration agent."));
    }

    @Test
    void copilotOutputIncludesFrontmatterAndPromptBody() throws IOException {
        Path outputDirectory = generatedOutputDirectory();
        service.renderCatalog(projectPath("agents"), outputDirectory);

        String rendered = Files.readString(
            outputDirectory.resolve(Path.of("copilot", ".github", "agents", "reviewer.agent.md"))
        );

        assertTrue(rendered.startsWith("---"));
        assertTrue(rendered.contains("name: \"Reviewer\""));
        assertTrue(rendered.contains("model: \"gpt-5\""));
        assertTrue(rendered.contains("tools:"));
        assertTrue(rendered.contains("You are a pragmatic review agent."));
    }

    @Test
    void renderingIsDeterministicAcrossRepeatedRuns() throws IOException {
        Path firstRun = generatedOutputDirectory();
        Path secondRun = generatedOutputDirectory();

        service.renderCatalog(projectPath("agents"), firstRun);
        service.renderCatalog(projectPath("agents"), secondRun);

        assertEquals(readRenderedFiles(firstRun), readRenderedFiles(secondRun));
    }

    @Test
    void rejectsOutputDirectoriesOutsideTheConfiguredSafeRoot(@TempDir Path tempDir) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.renderCatalog(projectPath("agents"), tempDir.resolve("unsafe-output"))
        );

        assertTrue(exception.getMessage().contains("Catalog output directory must be located under"));
    }

    private Map<String, String> readRenderedFiles(Path rootDirectory) throws IOException {
        try (var paths = Files.walk(rootDirectory)) {
            return paths.filter(Files::isRegularFile)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toMap(
                    path -> rootDirectory.relativize(path).toString().replace('\\', '/'),
                    this::readStringUnchecked,
                    (left, right) -> right,
                    java.util.LinkedHashMap::new
                ));
        }
    }

    private String readStringUnchecked(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read rendered file %s".formatted(path), exception);
        }
    }

    private Path projectPath(String relativePath) {
        return Path.of(System.getProperty("user.dir"), relativePath).toAbsolutePath().normalize();
    }

    private Path generatedOutputDirectory() throws IOException {
        Path testOutputRoot = projectPath(Path.of("build", "rendered", "test").toString());
        Files.createDirectories(testOutputRoot);
        return Files.createTempDirectory(testOutputRoot, "renderer-");
    }
}
