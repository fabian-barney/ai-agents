package dev.fabianbarney.aiagents.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AgentDefinition(
    @NotBlank(message = "must be non-blank") String id,
    @NotBlank(message = "must be non-blank") String name,
    @NotBlank(message = "must be non-blank") String purpose,
    @NotEmpty(message = "must be a non-empty list") List<@NotBlank(message = "must be non-blank") String> whenToUse,
    @NotEmpty(message = "must be a non-empty list") List<@NotBlank(message = "must be non-blank") String> boundaries,
    @NotBlank(message = "must be non-blank") String prompt,
    List<@NotBlank(message = "must be non-blank") String> aliases,
    List<@NotBlank(message = "must be non-blank") String> examples,
    List<@NotBlank(message = "must be non-blank") String> toolHints,
    List<@NotBlank(message = "must be non-blank") String> notes,
    List<@Valid PreferredModel> preferredModels,
    @Valid PlatformOverrides platformOverrides
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
