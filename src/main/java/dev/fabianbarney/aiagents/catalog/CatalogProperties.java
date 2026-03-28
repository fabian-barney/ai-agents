package dev.fabianbarney.aiagents.catalog;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Validated
@ConfigurationProperties("catalog")
public class CatalogProperties {

    @NotNull
    private Path input = Path.of("agents");

    @NotNull
    private Path output = Path.of("build", "rendered");

    public Path getInput() {
        return input;
    }

    public void setInput(Path input) {
        this.input = input;
    }

    public Path getOutput() {
        return output;
    }

    public void setOutput(Path output) {
        this.output = output;
    }
}
