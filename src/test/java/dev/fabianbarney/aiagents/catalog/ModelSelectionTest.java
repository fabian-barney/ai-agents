package dev.fabianbarney.aiagents.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelSelectionTest {

    @Test
    void codexUsesTheFirstCompatiblePreferredModel() {
        AgentDefinition agent = new AgentDefinition(
            "selector",
            "Selector",
            "Select a compatible model",
            List.of("testing"),
            List.of("stay focused"),
            "Prompt",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(
                new PreferredModel("github", "gpt-5", null),
                new PreferredModel("openai", "gpt-5.4", "high"),
                new PreferredModel("openai", "gpt-5.4-mini", "medium")
            ),
            PlatformOverrides.empty()
        );

        String rendered = new CodexRenderer().renderContent(agent);
        assertTrue(rendered.contains("model = \"gpt-5.4\""));
        assertTrue(rendered.contains("model_reasoning_effort = \"high\""));
    }

    @Test
    void copilotFallsBackToItsDefaultWhenNoCompatibleModelExists() {
        AgentDefinition agent = new AgentDefinition(
            "selector",
            "Selector",
            "Select a compatible model",
            List.of("testing"),
            List.of("stay focused"),
            "Prompt",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(new PreferredModel("openai", "gpt-5.4", "high")),
            PlatformOverrides.empty()
        );

        String rendered = new CopilotRenderer().renderContent(agent);
        assertTrue(rendered.contains("model: \"gpt-5\""));
    }
}
