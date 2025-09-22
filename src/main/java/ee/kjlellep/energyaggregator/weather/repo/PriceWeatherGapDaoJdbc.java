package ee.kjlellep.energyaggregator.weather.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class PriceWeatherGapDaoJdbc implements PriceWeatherGapDao {
    private final JdbcTemplate jdbc;
    public PriceWeatherGapDaoJdbc(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<LocalDate> fetchMissingDays(int limit) {
        final String sql = """
            WITH price_days AS (
                SELECT local_day AS day
                FROM electricity_price_hourly
                GROUP BY 1
            )
            SELECT pd.day
            FROM price_days pd
            LEFT JOIN weather_daily w ON w.day = pd.day
            WHERE w.day IS NULL
            ORDER BY pd.day
            LIMIT ?
        """;
        return jdbc.query(sql, (rs, i) -> rs.getObject("day", LocalDate.class), limit);
    }

    @Override
    public List<Range> fetchMissingRanges(int limit) {
        final String sql = """
            WITH price_days AS (
                SELECT local_day AS day
                FROM electricity_price_hourly
                GROUP BY 1
            ),
            missing AS (
                SELECT pd.day
                FROM price_days pd
                LEFT JOIN weather_daily w ON w.day = pd.day
                WHERE w.day IS NULL
            ),
            numbered AS (
                SELECT
                    day,
                    day - (ROW_NUMBER() OVER (ORDER BY day)) * INTERVAL '1 day' AS grp
                FROM missing
            )
            SELECT MIN(day) AS start_day, MAX(day) AS end_day, COUNT(*) AS days
            FROM numbered
            GROUP BY grp
            ORDER BY start_day
            LIMIT ?
        """;
        return jdbc.query(sql, (rs, i) ->
            new Range(
                rs.getObject("start_day", LocalDate.class),
                rs.getObject("end_day", LocalDate.class),
                rs.getInt("days")
            ),
            limit
        );
    }
}
