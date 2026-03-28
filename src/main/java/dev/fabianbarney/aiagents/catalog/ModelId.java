package dev.fabianbarney.aiagents.catalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;

public record ModelId(
    @NotBlank(message = "must be non-blank") String value
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ModelId {
    }

    @JsonValue
    public String asString() {
        return value;
    }

    public boolean isBlank() {
        return value == null || value.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }
}
