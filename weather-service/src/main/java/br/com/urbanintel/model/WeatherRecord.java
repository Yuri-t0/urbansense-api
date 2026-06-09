package br.com.urbanintel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double temperature;
    private Double feelsLike;
    private Integer humidity;
    private Double windSpeed;
    private Double rainMm;          
    private Integer uvIndex;
    private String condition;       
    private String description;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.recordedAt == null) this.recordedAt = LocalDateTime.now();
    }
}
