package ee.kjlellep.energyaggregator.query.service;

import ee.kjlellep.energyaggregator.query.repository.AggregatorQueryRepository;
import ee.kjlellep.energyaggregator.query.web.dto.DailyMetricsDto;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AggregatorQueryService {
    private static final int MAX_RANGE_DAYS = 5 * 365;

    private final AggregatorQueryRepository repository;

    public AggregatorQueryService(AggregatorQueryRepository repository) {
        this.repository = repository;
    }

    public List<DailyMetricsDto> query(LocalDate startDate, LocalDate endDate) {
        this.validateRange(startDate, endDate);

        List<DailyMetricsDto> rows = this.repository.fetchByRange(startDate, endDate);

        return rows.stream()
            .map(row -> new DailyMetricsDto(
                row.date(),
                row.averageTemperature() == null
                    ? null
                    : BigDecimal.valueOf(row.averageTemperature())
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue(),
                row.averagePrice() == null
                    ? null
                    : row.averagePrice().setScale(2, RoundingMode.HALF_UP)
            ))
            .toList();
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate.");
        }
        long span = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (span > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("Requested date range is too large.");
        }
    }
}
