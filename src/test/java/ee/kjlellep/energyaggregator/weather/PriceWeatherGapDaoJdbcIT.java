package ee.kjlellep.energyaggregator.weather;

import ee.kjlellep.energyaggregator.testsupport.PostgresTestBase;
import ee.kjlellep.energyaggregator.weather.repo.PriceWeatherGapDao;
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
class PriceWeatherGapDaoJdbcIT extends PostgresTestBase {
    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PriceWeatherGapDao gapDao;

    @Test
    void fetchMissingDaysAndRanges_respectPriceDaysVsWeatherDaily() {
        insertPriceHour(LocalDateTime.of(2024, 1, 1, 12, 0), "10.0000");
        insertPriceHour(LocalDateTime.of(2024, 1, 1, 13, 0), "11.0000");
        insertPriceHour(LocalDateTime.of(2024, 1, 2, 12, 0), "12.0000");
        insertPriceHour(LocalDateTime.of(2024, 1, 4, 12, 0), "14.0000");

        List<LocalDate> missing = gapDao.fetchMissingDays(10);
        assertThat(missing).containsExactly(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 2),
            LocalDate.of(2024, 1, 4)
        );

        insertWeatherDaily(LocalDate.of(2024, 1, 2), 5.0);

        missing = gapDao.fetchMissingDays(10);
        assertThat(missing).containsExactly(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 4)
        );

        var ranges = gapDao.fetchMissingRanges(10);
        assertThat(ranges).hasSize(2);
        assertThat(ranges.get(0).start()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(ranges.get(0).end()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(ranges.get(0).days()).isEqualTo(1);
        assertThat(ranges.get(1).start()).isEqualTo(LocalDate.of(2024, 1, 4));
        assertThat(ranges.get(1).end()).isEqualTo(LocalDate.of(2024, 1, 4));
        assertThat(ranges.get(1).days()).isEqualTo(1);

        insertWeatherDaily(LocalDate.of(2024, 1, 1), 3.3);
        missing = gapDao.fetchMissingDays(10);
        assertThat(missing).containsExactly(LocalDate.of(2024, 1, 4));
    }

    private void insertPriceHour(LocalDateTime tsUtc, String price) {
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
