package dev.fabianbarney.aiagents.catalog;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.TestAbortedException;

import java.nio.file.FileSystemException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertTrue(rendered.contains("developer_instructions = \"You are the implementation specialist.\\n"));
        assertFalse(rendered.contains("developer_instructions = '''"));
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

        assertMessageContains(exception, "Catalog output directory must be located under");
    }

    @Test
    void rejectsRendererPathsThatEscapeThePreparedOutputRoot(@TempDir Path tempDir) throws IOException {
        writeAgent(tempDir, "escape.yaml", agentDefinition("../../../../outside", "Prompt"));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.renderCatalog(tempDir, generatedOutputDirectory())
        );

        assertMessageContains(exception, "Renderer output path must stay under");
    }

    @Test
    void rejectsAbsoluteRendererOutputSubpaths() throws IOException {
        rendererProperties.getCodex().setOutputDirectory(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.renderCatalog(projectPath("agents"), generatedOutputDirectory())
        );

        assertMessageContains(exception, "Renderer output path must be relative");
    }

    @Test
    void rejectsSymlinkedOutputDirectoriesThatEscapeTheAllowedRoot(@TempDir Path tempDir) throws IOException {
        Path symlinkParent = Files.createTempDirectory(testOutputRoot(), "renderer-symlink-");
        Path externalTarget = Files.createDirectories(tempDir.resolve("external-target"));
        Path symlink = createDirectorySymlinkOrSkip(symlinkParent.resolve("outside-link"), externalTarget);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.renderCatalog(projectPath("agents"), symlink.resolve("nested"))
        );

        assertMessageContains(exception, "must not traverse symbolic links");
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
        return Files.createTempDirectory(testOutputRoot(), "renderer-");
    }

    private Path testOutputRoot() throws IOException {
        Path testOutputRoot = projectPath(Path.of("build", "rendered", "test").toString());
        Files.createDirectories(testOutputRoot);
        return testOutputRoot;
    }

    private Path createDirectorySymlinkOrSkip(Path link, Path target) throws IOException {
        try {
            return Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | SecurityException exception) {
            throw new TestAbortedException("Symbolic links are unavailable in this environment", exception);
        } catch (FileSystemException exception) {
            throw new TestAbortedException("Symbolic links are unavailable in this environment", exception);
        }
    }

    private void writeAgent(Path directory, String fileName, String content) throws IOException {
        Files.writeString(directory.resolve(fileName), content.stripIndent());
    }

    private String agentDefinition(String id, String prompt) {
        return """
            id: %s
            name: Test Agent
            purpose: Test rendering
            whenToUse:
              - testing
            boundaries:
              - stay focused
            prompt: %s
            """.formatted(id, prompt);
    }

    private void assertMessageContains(IllegalArgumentException exception, String expectedFragment) {
        String message = Objects.requireNonNull(exception.getMessage());
        assertTrue(message.contains(expectedFragment));
    }
}
