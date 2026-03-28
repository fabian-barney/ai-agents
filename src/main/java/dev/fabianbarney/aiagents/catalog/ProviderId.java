package dev.fabianbarney.aiagents.catalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;

public record ProviderId(
    @NotBlank(message = "must be non-blank") String value
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ProviderId {
    }

    @JsonValue
    public String asString() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
