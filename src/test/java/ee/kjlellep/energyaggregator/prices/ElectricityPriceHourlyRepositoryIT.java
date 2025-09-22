package ee.kjlellep.energyaggregator.prices;

import ee.kjlellep.energyaggregator.prices.domain.ElectricityPriceHourly;
import ee.kjlellep.energyaggregator.prices.repo.ElectricityPriceHourlyRepository;
import ee.kjlellep.energyaggregator.testsupport.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElectricityPriceHourlyRepositoryIT extends PostgresTestBase {
    @Autowired
    ElectricityPriceHourlyRepository repo;

    @Test
    void existsAndRangeQueryWork() {
        LocalDateTime h1 = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime h2 = h1.plusHours(1);
        LocalDateTime h3 = h1.plusHours(2);
        LocalDateTime h4 = LocalDateTime.of(2024, 1, 2, 12, 0);

        repo.save(new ElectricityPriceHourly(h1, new BigDecimal("50.0000")));
        repo.save(new ElectricityPriceHourly(h2, new BigDecimal("55.1234")));
        repo.save(new ElectricityPriceHourly(h3, new BigDecimal("60.0000")));
        repo.save(new ElectricityPriceHourly(h4, new BigDecimal("42.4242")));

        assertThat(repo.existsByTsUtc(h2)).isTrue();
        assertThat(repo.existsByTsUtc(h2.plusHours(10))).isFalse();

        List<ElectricityPriceHourly> range = repo.findAllByTsUtcBetweenOrderByTsUtcAsc(h1, h3);

        assertThat(range).hasSize(3);
        assertThat(range.get(0).getTsUtc()).isEqualTo(h1);
        assertThat(range.get(1).getTsUtc()).isEqualTo(h2);
        assertThat(range.get(2).getTsUtc()).isEqualTo(h3);
    }

    @Test
    void upsertOverwritesExistingValueOnSameTimestamp() {
        LocalDateTime ts = LocalDateTime.of(2024, 2, 10, 0, 0);

        repo.save(new ElectricityPriceHourly(ts, new BigDecimal("100.0000")));

        assertThat(repo.existsByTsUtc(ts)).isTrue();
        BigDecimal afterInsert = repo.findById(ts).orElseThrow().getNpsEesti();
        assertThat(afterInsert).isEqualByComparingTo("100.0000");

        repo.save(new ElectricityPriceHourly(ts, new BigDecimal("123.4567")));

        BigDecimal afterUpdate = repo.findById(ts).orElseThrow().getNpsEesti();
        assertThat(afterUpdate).isEqualByComparingTo("123.4567");
    }
}
