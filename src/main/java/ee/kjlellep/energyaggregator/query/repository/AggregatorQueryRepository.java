package ee.kjlellep.energyaggregator.query.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ee.kjlellep.energyaggregator.query.web.dto.DailyMetricsDto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class AggregatorQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AggregatorQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DailyMetricsDto> fetchByRange(LocalDate startDate, LocalDate endDate) {
        final String sql = """
            WITH price_daily AS (
                SELECT
                    local_day,
                    AVG(nps_eesti) AS avg_price
                FROM electricity_price_hourly
                WHERE local_day BETWEEN :startDate::date AND :endDate::date
                GROUP BY local_day
            )
            SELECT
                w.day        AS date,
                w.avg_temp_c AS average_temperature,
                p.avg_price  AS average_price
            FROM weather_daily w
            INNER JOIN price_daily p
                    ON p.local_day = w.day
            WHERE w.day BETWEEN :startDate::date AND :endDate::date
            ORDER BY w.day ASC
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("startDate", startDate)
            .addValue("endDate", endDate);

        return this.jdbc.query(sql, params, new DailyMetricsRowMapper());
    }

    private static class DailyMetricsRowMapper implements RowMapper<DailyMetricsDto> {
        @Override
        public DailyMetricsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            LocalDate date = rs.getObject("date", LocalDate.class);
            Double avgTemp = (Double) rs.getObject("average_temperature");
            BigDecimal avgPrice = (BigDecimal) rs.getObject("average_price");
            return new DailyMetricsDto(date, avgTemp, avgPrice);
        }
    }
}
