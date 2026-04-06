package media.barney.ai.agent.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogRunner implements ApplicationRunner {

    private final CatalogProperties properties;
    private final AgentCatalogService service;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        service.renderCatalog(
            properties.getInput().toAbsolutePath().normalize(),
            properties.getOutput().toAbsolutePath().normalize()
        );
    }
}
