package media.barney.ai.agent.catalog;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("catalog.renderers")
public class RendererProperties {

    private final Codex codex = new Codex();
    private final Claude claude = new Claude();
    private final Copilot copilot = new Copilot();

    public Codex getCodex() {
        return codex;
    }

    public Claude getClaude() {
        return claude;
    }

    public Copilot getCopilot() {
        return copilot;
    }

    public static final class Codex {
        private ProviderId compatibleProvider = new ProviderId("openai");
        private ModelId defaultModel = new ModelId("gpt-5.4");
        private Path outputDirectory = Path.of("codex", ".codex", "agents");
        private String fileSuffix = ".toml";
        private String nameKey = "name";
        private String descriptionKey = "description";
        private String modelKey = "model";
        private String modelReasoningEffortKey = "model_reasoning_effort";
        private String sandboxModeKey = "sandbox_mode";
        private String mcpServersKey = "mcp_servers";
        private String nicknameCandidatesKey = "nickname_candidates";
        private String developerInstructionsKey = "developer_instructions";

        public ProviderId getCompatibleProvider() {
            return compatibleProvider;
        }

        public void setCompatibleProvider(ProviderId compatibleProvider) {
            this.compatibleProvider = compatibleProvider;
        }

        public ModelId getDefaultModel() {
            return defaultModel;
        }

        public void setDefaultModel(ModelId defaultModel) {
            this.defaultModel = defaultModel;
        }

        public Path getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        public String getFileSuffix() {
            return fileSuffix;
        }

        public void setFileSuffix(String fileSuffix) {
            this.fileSuffix = fileSuffix;
        }

        public String getNameKey() {
            return nameKey;
        }

        public void setNameKey(String nameKey) {
            this.nameKey = nameKey;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }

        public void setDescriptionKey(String descriptionKey) {
            this.descriptionKey = descriptionKey;
        }

        public String getModelKey() {
            return modelKey;
        }

        public void setModelKey(String modelKey) {
            this.modelKey = modelKey;
        }

        public String getModelReasoningEffortKey() {
            return modelReasoningEffortKey;
        }

        public void setModelReasoningEffortKey(String modelReasoningEffortKey) {
            this.modelReasoningEffortKey = modelReasoningEffortKey;
        }

        public String getSandboxModeKey() {
            return sandboxModeKey;
        }

        public void setSandboxModeKey(String sandboxModeKey) {
            this.sandboxModeKey = sandboxModeKey;
        }

        public String getMcpServersKey() {
            return mcpServersKey;
        }

        public void setMcpServersKey(String mcpServersKey) {
            this.mcpServersKey = mcpServersKey;
        }

        public String getNicknameCandidatesKey() {
            return nicknameCandidatesKey;
        }

        public void setNicknameCandidatesKey(String nicknameCandidatesKey) {
            this.nicknameCandidatesKey = nicknameCandidatesKey;
        }

        public String getDeveloperInstructionsKey() {
            return developerInstructionsKey;
        }

        public void setDeveloperInstructionsKey(String developerInstructionsKey) {
            this.developerInstructionsKey = developerInstructionsKey;
        }
    }

    public static final class Claude {
        private ProviderId compatibleProvider = new ProviderId("anthropic");
        private ModelId defaultModel = new ModelId("claude-sonnet-4-5");
        private Path outputDirectory = Path.of("claude", ".claude", "agents");
        private String fileSuffix = ".md";
        private String frontmatterDelimiter = "---";
        private String nameKey = "name";
        private String descriptionKey = "description";
        private String toolsKey = "tools";

        public ProviderId getCompatibleProvider() {
            return compatibleProvider;
        }

        public void setCompatibleProvider(ProviderId compatibleProvider) {
            this.compatibleProvider = compatibleProvider;
        }

        public ModelId getDefaultModel() {
            return defaultModel;
        }

        public void setDefaultModel(ModelId defaultModel) {
            this.defaultModel = defaultModel;
        }

        public Path getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        public String getFileSuffix() {
            return fileSuffix;
        }

        public void setFileSuffix(String fileSuffix) {
            this.fileSuffix = fileSuffix;
        }

        public String getFrontmatterDelimiter() {
            return frontmatterDelimiter;
        }

        public void setFrontmatterDelimiter(String frontmatterDelimiter) {
            this.frontmatterDelimiter = frontmatterDelimiter;
        }

        public String getNameKey() {
            return nameKey;
        }

        public void setNameKey(String nameKey) {
            this.nameKey = nameKey;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }

        public void setDescriptionKey(String descriptionKey) {
            this.descriptionKey = descriptionKey;
        }

        public String getToolsKey() {
            return toolsKey;
        }

        public void setToolsKey(String toolsKey) {
            this.toolsKey = toolsKey;
        }
    }

    public static final class Copilot {
        private ProviderId compatibleProvider = new ProviderId("github");
        private ModelId defaultModel = new ModelId("gpt-5");
        private Path outputDirectory = Path.of("copilot", ".github", "agents");
        private String fileSuffix = ".agent.md";
        private String frontmatterDelimiter = "---";
        private String nameKey = "name";
        private String descriptionKey = "description";
        private String modelKey = "model";
        private String targetKey = "target";
        private String toolsKey = "tools";
        private String mcpServersKey = "mcp-servers";

        public ProviderId getCompatibleProvider() {
            return compatibleProvider;
        }

        public void setCompatibleProvider(ProviderId compatibleProvider) {
            this.compatibleProvider = compatibleProvider;
        }

        public ModelId getDefaultModel() {
            return defaultModel;
        }

        public void setDefaultModel(ModelId defaultModel) {
            this.defaultModel = defaultModel;
        }

        public Path getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        public String getFileSuffix() {
            return fileSuffix;
        }

        public void setFileSuffix(String fileSuffix) {
            this.fileSuffix = fileSuffix;
        }

        public String getFrontmatterDelimiter() {
            return frontmatterDelimiter;
        }

        public void setFrontmatterDelimiter(String frontmatterDelimiter) {
            this.frontmatterDelimiter = frontmatterDelimiter;
        }

        public String getNameKey() {
            return nameKey;
        }

        public void setNameKey(String nameKey) {
            this.nameKey = nameKey;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }

        public void setDescriptionKey(String descriptionKey) {
            this.descriptionKey = descriptionKey;
        }

        public String getModelKey() {
            return modelKey;
        }

        public void setModelKey(String modelKey) {
            this.modelKey = modelKey;
        }

        public String getTargetKey() {
            return targetKey;
        }

        public void setTargetKey(String targetKey) {
            this.targetKey = targetKey;
        }

        public String getToolsKey() {
            return toolsKey;
        }

        public void setToolsKey(String toolsKey) {
            this.toolsKey = toolsKey;
        }

        public String getMcpServersKey() {
            return mcpServersKey;
        }

        public void setMcpServersKey(String mcpServersKey) {
            this.mcpServersKey = mcpServersKey;
        }
    }
}
