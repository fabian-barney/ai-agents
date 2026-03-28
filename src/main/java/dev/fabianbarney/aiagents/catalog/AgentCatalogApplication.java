package dev.fabianbarney.aiagents.catalog;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableConfigurationProperties({CatalogProperties.class, RendererProperties.class})
public class AgentCatalogApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AgentCatalogApplication.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }
}
