package dev.fabianbarney.aiagents.catalog;

public record PreferredModel(
    String provider,
    String model,
    String reasoningEffort
) {
}
