package ee.kjlellep.energyaggregator.prices.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "electricity_price_hourly")
public class ElectricityPriceHourly {
    @Id
    @Column(name = "ts_utc", nullable = false)
    private LocalDateTime tsUtc;

    @Column(name = "nps_eesti", nullable = false, precision = 10, scale = 4)
    private BigDecimal npsEesti;

    protected ElectricityPriceHourly() {}

    public ElectricityPriceHourly(LocalDateTime tsUtc, BigDecimal npsEesti) {
        this.tsUtc = tsUtc;
        this.npsEesti = npsEesti;
    }

    public LocalDateTime getTsUtc() { return this.tsUtc; }
    public BigDecimal getNpsEesti() { return this.npsEesti; }
    public void setNpsEesti(BigDecimal v) { this.npsEesti = v; }
}
