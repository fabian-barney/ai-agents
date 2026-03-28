package dev.fabianbarney.aiagents.catalog;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogRunner implements ApplicationRunner {

    private final CatalogProperties properties;
    private final AgentCatalogService service;

    public CatalogRunner(CatalogProperties properties, AgentCatalogService service) {
        this.properties = properties;
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        service.renderCatalog(
            properties.getInput().toAbsolutePath().normalize(),
            properties.getOutput().toAbsolutePath().normalize()
        );
    }
}
