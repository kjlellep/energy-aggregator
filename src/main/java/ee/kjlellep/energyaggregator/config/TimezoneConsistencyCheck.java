package ee.kjlellep.energyaggregator.config;

import ee.kjlellep.energyaggregator.prices.config.PriceImportProperties;
import ee.kjlellep.energyaggregator.weather.config.WeatherImportProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimezoneConsistencyCheck {
    public TimezoneConsistencyCheck(PriceImportProperties prices, WeatherImportProperties weather) {
        if (!prices.timezone().equals(weather.timezone())) {
            throw new IllegalStateException(
                "Prices and weather timezones must match: " +
                prices.timezone() + " vs " + weather.timezone()
            );
        }
    }
}
