package ee.kjlellep.energyaggregator.weather.repo;

import java.time.LocalDate;
import java.util.List;

public interface PriceWeatherGapDao {
    List<LocalDate> fetchMissingDays(int limit);
    List<Range> fetchMissingRanges(int limit);
    record Range(LocalDate start, LocalDate end, int days) {}
}
