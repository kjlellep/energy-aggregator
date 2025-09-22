package ee.kjlellep.energyaggregator.weather.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DailyData(
    List<String> time,

    @JsonProperty("temperature_2m_mean")
    List<Double> temperature2mMean
) {}
