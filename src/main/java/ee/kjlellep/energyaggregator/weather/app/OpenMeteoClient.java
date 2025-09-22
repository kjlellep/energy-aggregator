package ee.kjlellep.energyaggregator.weather.app;

import ee.kjlellep.energyaggregator.weather.app.dto.OpenMeteoDailyResponse;
import ee.kjlellep.energyaggregator.weather.config.WeatherImportProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Service
public class OpenMeteoClient {
    private final RestClient http;
    private final WeatherImportProperties props;

    public OpenMeteoClient(RestClient.Builder builder, WeatherImportProperties props) {
        this.http = builder.baseUrl("https://archive-api.open-meteo.com").build();
        this.props = props;
    }

    public OpenMeteoDailyResponse fetchDailyMean(LocalDate start, LocalDate end) {
        return this.http.get()
            .uri(uri -> uri.path("/v1/archive")
            .queryParam("latitude", this.props.latitude())
            .queryParam("longitude", this.props.longitude())
            .queryParam("start_date", start)
            .queryParam("end_date", end)
            .queryParam("daily", "temperature_2m_mean")
            .queryParam("timezone", this.props.timezone())
            .build())
            .retrieve()
            .body(OpenMeteoDailyResponse.class);
    }
}
