package ee.kjlellep.energyaggregator.weather.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Configuration
@EnableScheduling
class WeatherScheduling {}

@Component
class WeatherDailyCron {
    private static final Logger log = LoggerFactory.getLogger(WeatherDailyCron.class);
    private final WeatherDailyRangeFiller filler;

    public WeatherDailyCron(WeatherDailyRangeFiller filler) {
        this.filler = filler;
    }

    @Scheduled(cron = "0 * * * * *")
    public void run() {
        try {
            int n = this.filler.fillOnce();
            if (n > 0) log.info("weather_daily upserted {} day(s)", n);
        } catch (Exception e) {
            log.warn("Weather daily fill failed", e);
        }
    }
}
