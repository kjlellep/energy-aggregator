package ee.kjlellep.energyaggregator.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.weather")
public record WeatherImportProperties(
    double latitude,
    double longitude,
    String timezone,
    int maxRangesPerRun,
    int maxDaysPerCall
) {}
