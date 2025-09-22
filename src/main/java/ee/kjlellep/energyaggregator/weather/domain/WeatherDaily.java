package ee.kjlellep.energyaggregator.weather.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_daily")
public class WeatherDaily {
    @Id
    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "avg_temp_c", nullable = false)
    private Double avgTempC;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt = LocalDateTime.now();

    public LocalDate getDay() { return this.day; }
    public void setDay(LocalDate day) { this.day = day; }
    public Double getAvgTempC() { return this.avgTempC; }
    public void setAvgTempC(Double avgTempC) { this.avgTempC = avgTempC; }
    public LocalDateTime getComputedAt() { return this.computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
}
