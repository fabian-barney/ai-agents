package media.barney.ai.agent.catalog;

import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record PlatformOverrides(
    @Valid CodexOverrides codex,
    @Valid ClaudeOverrides claude,
    @Valid CopilotOverrides copilot
) {
    public PlatformOverrides {
        codex = codex == null ? CodexOverrides.empty() : codex;
        claude = claude == null ? ClaudeOverrides.empty() : claude;
        copilot = copilot == null ? CopilotOverrides.empty() : copilot;
    }

    public static PlatformOverrides empty() {
        return new PlatformOverrides(CodexOverrides.empty(), ClaudeOverrides.empty(), CopilotOverrides.empty());
    }
}

record CodexOverrides(
    @Nullable String description,
    @Valid @Nullable ModelId model,
    @Nullable String modelReasoningEffort,
    @Nullable String sandboxMode,
    List<String> mcpServers,
    List<String> nicknameCandidates
) {
    CodexOverrides {
        mcpServers = mcpServers == null ? List.of() : List.copyOf(mcpServers);
        nicknameCandidates = nicknameCandidates == null ? List.of() : List.copyOf(nicknameCandidates);
    }

    static CodexOverrides empty() {
        return new CodexOverrides(null, null, null, null, List.of(), List.of());
    }
}

record ClaudeOverrides(
    @Nullable String description,
    List<String> tools
) {
    ClaudeOverrides {
        tools = tools == null ? List.of() : List.copyOf(tools);
    }

    static ClaudeOverrides empty() {
        return new ClaudeOverrides(null, List.of());
    }
}

record CopilotOverrides(
    @Nullable String description,
    @Valid @Nullable ModelId model,
    List<String> tools,
    List<String> mcpServers,
    @Nullable String target
) {
    CopilotOverrides {
        tools = tools == null ? List.of() : List.copyOf(tools);
        mcpServers = mcpServers == null ? List.of() : List.copyOf(mcpServers);
    }

    static CopilotOverrides empty() {
        return new CopilotOverrides(null, null, List.of(), List.of(), null);
    }
}
