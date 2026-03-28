package dev.fabianbarney.aiagents.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelSelectionTest {

    @Test
    void codexUsesTheFirstCompatiblePreferredModel() {
        RendererProperties rendererProperties = new RendererProperties();
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
                new PreferredModel(new ProviderId("github"), new ModelId("gpt-5"), null),
                new PreferredModel(new ProviderId("openai"), new ModelId("gpt-5.4"), "high"),
                new PreferredModel(new ProviderId("openai"), new ModelId("gpt-5.4-mini"), "medium")
            ),
            PlatformOverrides.empty()
        );

        String rendered = new CodexRenderer(rendererProperties).renderContent(agent);
        assertTrue(rendered.contains("model = \"gpt-5.4\""));
        assertTrue(rendered.contains("model_reasoning_effort = \"high\""));
    }

    @Test
    void codexReasoningEffortComesFromTheSamePreferredModelEntry() {
        RendererProperties rendererProperties = new RendererProperties();
        AgentDefinition agent = new AgentDefinition(
            "selector",
            "Selector",
            "Keep model metadata aligned",
            List.of("testing"),
            List.of("stay focused"),
            "Prompt",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(
                new PreferredModel(new ProviderId("openai"), new ModelId("gpt-5.4"), null),
                new PreferredModel(new ProviderId("openai"), new ModelId("gpt-5.4-mini"), "medium")
            ),
            PlatformOverrides.empty()
        );

        String rendered = new CodexRenderer(rendererProperties).renderContent(agent);
        assertTrue(rendered.contains("model = \"gpt-5.4\""));
        assertTrue(!rendered.contains("model_reasoning_effort ="));
    }

    @Test
    void copilotFallsBackToItsDefaultWhenNoCompatibleModelExists() {
        RendererProperties rendererProperties = new RendererProperties();
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
            List.of(new PreferredModel(new ProviderId("openai"), new ModelId("gpt-5.4"), "high")),
            PlatformOverrides.empty()
        );

        String rendered = new CopilotRenderer(rendererProperties).renderContent(agent);
        assertTrue(rendered.contains("model: \"gpt-5\""));
    }

    @Test
    void renderersUseConfiguredDefaultStrings() {
        RendererProperties rendererProperties = new RendererProperties();
        rendererProperties.getCopilot().setDefaultModel(new ModelId("custom-copilot-model"));
        rendererProperties.getCopilot().setModelKey("preferred-model");

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
            List.of(new PreferredModel(new ProviderId("openai"), new ModelId("gpt-5.4"), "high")),
            PlatformOverrides.empty()
        );

        String rendered = new CopilotRenderer(rendererProperties).renderContent(agent);
        assertTrue(rendered.contains("preferred-model: \"custom-copilot-model\""));
    }
}
