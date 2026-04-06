package media.barney.ai.agent.catalog;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static media.barney.ai.agent.catalog.TestAssertions.assertMessageContains;

class AgentDefinitionLoaderTest {

    private final AgentDefinitionLoader loader = new AgentDefinitionLoader(
        Validation.buildDefaultValidatorFactory().getValidator()
    );

    @Test
    void loadsTheCanonicalAgentsFromTheRepository() throws IOException {
        List<AgentDefinition> definitions = loader.load(projectPath("agents"));

        assertEquals(4, definitions.size());
        assertEquals(
            List.of("explorer", "implementer", "orchestrator", "reviewer"),
            definitions.stream().map(AgentDefinition::id).toList()
        );
    }

    @Test
    void rejectsMissingRequiredFields(@TempDir Path tempDir) throws IOException {
        writeAgent(
            tempDir,
            "broken.yaml",
            """
            id: broken
            purpose: Missing required name
            whenToUse:
              - testing
            boundaries:
              - stay focused
            prompt: |
              Missing name.
            """
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loader.load(tempDir));
        assertMessageContains(exception, "name must be non-blank");
    }

    @Test
    void rejectsDuplicateAgentIds(@TempDir Path tempDir) throws IOException {
        String definition = """
            id: duplicate
            name: Duplicate
            purpose: Duplicate id test
            whenToUse:
              - testing
            boundaries:
              - stay focused
            prompt: |
              Duplicate.
            """;
        writeAgent(tempDir, "first.yaml", definition);
        writeAgent(tempDir, "second.yaml", definition);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loader.load(tempDir));
        assertMessageContains(exception, "Duplicate agent id 'duplicate'");
    }

    @Test
    void rejectsEmptyPreferredModelLists(@TempDir Path tempDir) throws IOException {
        writeAgent(
            tempDir,
            "broken.yaml",
            """
            id: empty-preferences
            name: Empty Preferences
            purpose: Invalid preferred model list
            whenToUse:
              - testing
            boundaries:
              - stay focused
            prompt: |
              Empty preference list.
            preferredModels: []
            """
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loader.load(tempDir));
        assertMessageContains(exception, "preferredModels must be omitted or contain at least one entry");
    }

    @Test
    void rejectsMalformedPreferredModelEntries(@TempDir Path tempDir) throws IOException {
        writeAgent(
            tempDir,
            "broken.yaml",
            """
            id: malformed-model
            name: Malformed Model
            purpose: Invalid preferred model entry
            whenToUse:
              - testing
            boundaries:
              - stay focused
            prompt: |
              Invalid model.
            preferredModels:
              - provider: openai
                model:
            """
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loader.load(tempDir));
        assertMessageContains(exception, "preferredModels.model must be non-blank");
    }

    private void writeAgent(Path directory, String fileName, String content) throws IOException {
        Files.writeString(directory.resolve(fileName), content.stripIndent());
    }

    private Path projectPath(String relativePath) {
        return Path.of(System.getProperty("user.dir"), relativePath).toAbsolutePath().normalize();
    }
}
