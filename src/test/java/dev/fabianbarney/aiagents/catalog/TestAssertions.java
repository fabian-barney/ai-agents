package dev.fabianbarney.aiagents.catalog;

import org.jspecify.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestAssertions {

    private TestAssertions() {
    }

    static void assertMessageContains(IllegalArgumentException exception, String expectedFragment) {
        @Nullable String message = exception.getMessage();
        assertNotNull(message);
        if (message == null) {
            return;
        }
        assertTrue(message.contains(expectedFragment));
    }
}
