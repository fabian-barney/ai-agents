package dev.fabianbarney.aiagents.catalog;

import jakarta.validation.Valid;

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
        return new PlatformOverrides(null, null, null);
    }
}

record CodexOverrides(
    String description,
    @Valid ModelId model,
    String modelReasoningEffort,
    String sandboxMode,
    List<String> mcpServers,
    List<String> nicknameCandidates
) {
    CodexOverrides {
        mcpServers = mcpServers == null ? List.of() : List.copyOf(mcpServers);
        nicknameCandidates = nicknameCandidates == null ? List.of() : List.copyOf(nicknameCandidates);
    }

    static CodexOverrides empty() {
        return new CodexOverrides(null, null, null, null, null, null);
    }
}

record ClaudeOverrides(
    String description,
    List<String> tools
) {
    ClaudeOverrides {
        tools = tools == null ? List.of() : List.copyOf(tools);
    }

    static ClaudeOverrides empty() {
        return new ClaudeOverrides(null, null);
    }
}

record CopilotOverrides(
    String description,
    @Valid ModelId model,
    List<String> tools,
    List<String> mcpServers,
    String target
) {
    CopilotOverrides {
        tools = tools == null ? List.of() : List.copyOf(tools);
        mcpServers = mcpServers == null ? List.of() : List.copyOf(mcpServers);
    }

    static CopilotOverrides empty() {
        return new CopilotOverrides(null, null, null, null, null);
    }
}
