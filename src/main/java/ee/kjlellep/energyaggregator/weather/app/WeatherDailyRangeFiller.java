package ee.kjlellep.energyaggregator.weather.app;

import ee.kjlellep.energyaggregator.weather.app.dto.OpenMeteoDailyResponse;
import ee.kjlellep.energyaggregator.weather.config.WeatherImportProperties;
import ee.kjlellep.energyaggregator.weather.repo.PriceWeatherGapDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class WeatherDailyRangeFiller {
    private static final String UPSERT_SQL = """
        INSERT INTO weather_daily(day, avg_temp_c)
        VALUES (?, ?)
        ON CONFLICT (day) DO UPDATE SET avg_temp_c = EXCLUDED.avg_temp_c
    """;

    private final PriceWeatherGapDao gaps;
    private final OpenMeteoClient client;
    private final JdbcTemplate jdbc;
    private final WeatherImportProperties props;
    private final TransactionTemplate tx;

    public WeatherDailyRangeFiller(
        PriceWeatherGapDao gaps,
        OpenMeteoClient client,
        JdbcTemplate jdbc,
        WeatherImportProperties props,
        PlatformTransactionManager txManager
    ) {
        this.gaps = gaps;
        this.client = client;
        this.jdbc = jdbc;
        this.props = props;
        this.tx = new TransactionTemplate(txManager);
    }

    public int fillOnce() {
        HashSet<LocalDate> missing = new HashSet<>(this.gaps.fetchMissingDays(10_000));
        if (missing.isEmpty()) return 0;

        List<PriceWeatherGapDao.Range> ranges = this.gaps.fetchMissingRanges(this.props.maxRangesPerRun());
        int inserted = 0;

        for (PriceWeatherGapDao.Range r : ranges) {
            for (
                LocalDate winStart = r.start();
                !winStart.isAfter(r.end());
                winStart = winStart.plusDays(this.props.maxDaysPerCall())
            ) {
                LocalDate winEnd = winStart.plusDays(this.props.maxDaysPerCall() - 1);
                if (winEnd.isAfter(r.end())) winEnd = r.end();

                OpenMeteoDailyResponse resp = this.client.fetchDailyMean(winStart, winEnd);
                if (resp == null || resp.daily() == null || resp.daily().time() == null) continue;

                List<String> dates = resp.daily().time();
                List<Double> means = resp.daily().temperature2mMean();
                if (means == null) continue;

                List<Object[]> batch = new ArrayList<>();
                for (int i = 0; i < dates.size(); i++) {
                    LocalDate day = LocalDate.parse(dates.get(i));
                    if (!missing.contains(day)) continue;
                    Double mean = means.get(i);
                    if (mean == null) continue;
                    batch.add(new Object[]{ day, mean });
                }

                if (!batch.isEmpty()) {
                    this.tx.executeWithoutResult(status -> this.jdbc.batchUpdate(UPSERT_SQL, batch));
                    inserted += batch.size();
                    for (Object[] row : batch) {
                        missing.remove((LocalDate) row[0]);
                    }
                }
            }
        }
        return inserted;
    }
}
