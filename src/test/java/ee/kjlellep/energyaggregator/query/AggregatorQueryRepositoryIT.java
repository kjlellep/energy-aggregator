package ee.kjlellep.energyaggregator.query;

import ee.kjlellep.energyaggregator.query.repository.AggregatorQueryRepository;
import ee.kjlellep.energyaggregator.query.web.dto.DailyMetricsDto;
import ee.kjlellep.energyaggregator.testsupport.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AggregatorQueryRepositoryIT extends PostgresTestBase {
    @Autowired AggregatorQueryRepository aggRepo;
    @Autowired JdbcTemplate jdbc;

    @Test
    void aggregatorQuery_joinsAndAveragesByDay() {
        LocalDate d1 = LocalDate.of(2024, 3, 1);
        LocalDate d2 = LocalDate.of(2024, 3, 2);
        LocalDate d3 = LocalDate.of(2024, 3, 3);

        insertPrice(LocalDateTime.of(2024, 3, 1,  0, 0), "100.0000");
        insertPrice(LocalDateTime.of(2024, 3, 1,  1, 0), "200.0000");
        insertPrice(LocalDateTime.of(2024, 3, 2, 12, 0), "50.0000");
        insertPrice(LocalDateTime.of(2024, 3, 3, 12, 0), "75.0000");

        insertWeatherDaily(d1, 1.0);
        insertWeatherDaily(d2, -5.5);

        List<DailyMetricsDto> out = aggRepo.fetchByRange(d1, d3);

        assertThat(out).hasSize(2);

        assertThat(out.get(0).date()).isEqualTo(d1);
        assertThat(out.get(0).averageTemperature()).isEqualTo(1.0);
        assertThat(out.get(0).averagePrice()).isEqualByComparingTo("150.0000");

        assertThat(out.get(1).date()).isEqualTo(d2);
        assertThat(out.get(1).averageTemperature()).isEqualTo(-5.5);
        assertThat(out.get(1).averagePrice()).isEqualByComparingTo("50.0000");
    }

    private void insertPrice(LocalDateTime tsUtc, String price) {
        jdbc.update(
            "INSERT INTO electricity_price_hourly (ts_utc, nps_eesti) VALUES (?, ?)",
            tsUtc, new BigDecimal(price)
        );
    }

    private void insertWeatherDaily(LocalDate day, double avgTemp) {
        jdbc.update(
            "INSERT INTO weather_daily (day, avg_temp_c, computed_at) VALUES (?, ?, now())",
            day, avgTemp
        );
    }
}
