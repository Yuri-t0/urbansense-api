package br.com.urbanintel.domain.port.out;

import br.com.urbanintel.domain.model.Weather;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WeatherRepositoryPort {
    Weather save(Weather weather);
    Optional<Weather> findLatestByCity(String city);
    List<Weather> findHeavyRainSince(double thresholdMm, LocalDateTime since);
    Double avgRainForHour(String city, int hour, LocalDateTime since);
    List<Weather> findRainEventsByCity(String city, LocalDateTime since);
}
