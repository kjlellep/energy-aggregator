package ee.kjlellep.energyaggregator.prices.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.prices.import")
public record PriceImportProperties(
    String timezone,
    String datetimePattern,
    int batchSize
) {}
