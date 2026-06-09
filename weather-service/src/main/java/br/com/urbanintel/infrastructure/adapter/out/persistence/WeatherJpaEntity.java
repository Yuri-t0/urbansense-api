package br.com.urbanintel.infrastructure.adapter.out.persistence;

import br.com.urbanintel.domain.model.Weather;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class WeatherJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String city;
    @Column(nullable = false) private Double latitude;
    @Column(nullable = false) private Double longitude;
    private Double temperature;
    private Double feelsLike;
    private Integer humidity;
    private Double windSpeed;
    private Double rainMm;
    private Integer uvIndex;
    private String condition;
    private String description;
    @Column(nullable = false) private LocalDateTime recordedAt;
    @Column(nullable = false) private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.recordedAt == null) this.recordedAt = LocalDateTime.now();
    }

    static WeatherJpaEntity fromDomain(Weather w) {
        return WeatherJpaEntity.builder()
                .id(w.id())
                .city(w.city())
                .latitude(w.latitude())
                .longitude(w.longitude())
                .temperature(w.temperature())
                .feelsLike(w.feelsLike())
                .humidity(w.humidity())
                .windSpeed(w.windSpeed())
                .rainMm(w.rainMm())
                .uvIndex(w.uvIndex())
                .condition(w.condition() != null ? w.condition().name() : null)
                .description(w.description())
                .recordedAt(w.recordedAt() != null ? w.recordedAt() : LocalDateTime.now())
                .build();
    }

    Weather toDomain() {
        Weather.WeatherCondition cond;
        try {
            cond = condition != null ? Weather.WeatherCondition.valueOf(condition)
                                     : Weather.WeatherCondition.UNKNOWN;
        } catch (IllegalArgumentException e) {
            cond = Weather.WeatherCondition.UNKNOWN;
        }
        return new Weather(id, city, latitude, longitude, temperature, feelsLike,
                humidity, windSpeed, rainMm, uvIndex, cond, description, recordedAt);
    }
}
