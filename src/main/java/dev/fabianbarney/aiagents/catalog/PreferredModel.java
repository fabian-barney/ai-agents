package dev.fabianbarney.aiagents.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

public record PreferredModel(
    @NotNull(message = "must be non-blank") @Valid ProviderId provider,
    @NotNull(message = "must be non-blank") @Valid ModelId model,
    @Nullable String reasoningEffort
) {
}
