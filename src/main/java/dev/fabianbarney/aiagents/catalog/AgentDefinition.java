package dev.fabianbarney.aiagents.catalog;

import java.util.List;

public record AgentDefinition(
    String id,
    String name,
    String purpose,
    List<String> whenToUse,
    List<String> boundaries,
    String prompt,
    List<String> aliases,
    List<String> examples,
    List<String> toolHints,
    List<String> notes,
    List<PreferredModel> preferredModels,
    PlatformOverrides platformOverrides
) {

    public AgentDefinition {
        whenToUse = immutableList(whenToUse);
        boundaries = immutableList(boundaries);
        aliases = immutableList(aliases);
        examples = immutableList(examples);
        toolHints = immutableList(toolHints);
        notes = immutableList(notes);
        preferredModels = preferredModelList(preferredModels);
        platformOverrides = platformOverrides == null ? PlatformOverrides.empty() : platformOverrides;
    }

    private static List<String> immutableList(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private static List<PreferredModel> preferredModelList(List<PreferredModel> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
