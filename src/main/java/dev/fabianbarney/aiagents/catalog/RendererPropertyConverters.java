package dev.fabianbarney.aiagents.catalog;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
final class ModelIdConverter implements Converter<String, ModelId> {

    @Override
    public ModelId convert(String source) {
        return new ModelId(source);
    }
}

@Component
@ConfigurationPropertiesBinding
final class ProviderIdConverter implements Converter<String, ProviderId> {

    @Override
    public ProviderId convert(String source) {
        return new ProviderId(source);
    }
}
