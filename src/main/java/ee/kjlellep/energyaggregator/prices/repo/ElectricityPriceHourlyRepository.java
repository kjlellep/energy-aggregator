package ee.kjlellep.energyaggregator.prices.repo;

import ee.kjlellep.energyaggregator.prices.domain.ElectricityPriceHourly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ElectricityPriceHourlyRepository extends JpaRepository<ElectricityPriceHourly, LocalDateTime> {
    List<ElectricityPriceHourly> findAllByTsUtcBetweenOrderByTsUtcAsc(LocalDateTime from, LocalDateTime to);

    boolean existsByTsUtc(LocalDateTime tsUtc);
}
