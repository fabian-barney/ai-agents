package dev.fabianbarney.aiagents.catalog;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class RendererIntegrationTest {

    private final AgentCatalogService service = new AgentCatalogService(
        new AgentDefinitionLoader(),
        List.of(new CodexRenderer(), new ClaudeRenderer(), new CopilotRenderer())
    );

    @Test
    void rendersAllExpectedFilesIntoTargetSpecificDirectories(@TempDir Path tempDir) throws IOException {
        service.renderCatalog(projectPath("agents"), tempDir);

        for (String agentId : List.of("orchestrator", "explorer", "implementer", "reviewer")) {
            assertTrue(Files.exists(tempDir.resolve(Path.of("codex", ".codex", "agents", agentId + ".toml"))));
            assertTrue(Files.exists(tempDir.resolve(Path.of("claude", ".claude", "agents", agentId + ".md"))));
            assertTrue(Files.exists(tempDir.resolve(Path.of("copilot", ".github", "agents", agentId + ".agent.md"))));
        }
    }

    @Test
    void codexOutputIncludesModelSelectionAndSandboxOverrides(@TempDir Path tempDir) throws IOException {
        service.renderCatalog(projectPath("agents"), tempDir);

        String rendered = Files.readString(
            tempDir.resolve(Path.of("codex", ".codex", "agents", "implementer.toml"))
        );

        assertTrue(rendered.contains("name = \"Implementer\""));
        assertTrue(rendered.contains("model = \"gpt-5.4\""));
        assertTrue(rendered.contains("model_reasoning_effort = \"medium\""));
        assertTrue(rendered.contains("sandbox_mode = \"workspace-write\""));
        assertTrue(rendered.contains("developer_instructions = '''"));
    }

    @Test
    void claudeOutputIncludesFrontmatterAndPromptBody(@TempDir Path tempDir) throws IOException {
        service.renderCatalog(projectPath("agents"), tempDir);

        String rendered = Files.readString(
            tempDir.resolve(Path.of("claude", ".claude", "agents", "explorer.md"))
        );

        assertTrue(rendered.startsWith("---"));
        assertTrue(rendered.contains("name: \"explorer\""));
        assertTrue(rendered.contains("description: \"Investigate code, configuration, and context"));
        assertTrue(rendered.contains("tools:"));
        assertTrue(rendered.contains("You are a focused exploration agent."));
    }

    @Test
    void copilotOutputIncludesFrontmatterAndPromptBody(@TempDir Path tempDir) throws IOException {
        service.renderCatalog(projectPath("agents"), tempDir);

        String rendered = Files.readString(
            tempDir.resolve(Path.of("copilot", ".github", "agents", "reviewer.agent.md"))
        );

        assertTrue(rendered.startsWith("---"));
        assertTrue(rendered.contains("name: \"Reviewer\""));
        assertTrue(rendered.contains("model: \"gpt-5\""));
        assertTrue(rendered.contains("tools:"));
        assertTrue(rendered.contains("You are a pragmatic review agent."));
    }

    @Test
    void renderingIsDeterministicAcrossRepeatedRuns(@TempDir Path tempDir) throws IOException {
        Path firstRun = tempDir.resolve("first");
        Path secondRun = tempDir.resolve("second");

        service.renderCatalog(projectPath("agents"), firstRun);
        service.renderCatalog(projectPath("agents"), secondRun);

        assertEquals(readRenderedFiles(firstRun), readRenderedFiles(secondRun));
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
}
