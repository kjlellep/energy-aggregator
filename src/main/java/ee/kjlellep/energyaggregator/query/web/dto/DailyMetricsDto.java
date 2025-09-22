package ee.kjlellep.energyaggregator.query.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "DailyMetrics", description = "Aggregated daily weather and electricity price metrics.")
public record DailyMetricsDto(
    @Schema(format = "date", example = "2024-01-24")
    LocalDate date,

    @Schema(description = "Average temperature (°C) for the day", example = "-1.2", nullable = true)
    Double averageTemperature,

    @Schema(description = "Average electricity price for the day", example = "35.50", nullable = true)
    BigDecimal averagePrice
) {}
