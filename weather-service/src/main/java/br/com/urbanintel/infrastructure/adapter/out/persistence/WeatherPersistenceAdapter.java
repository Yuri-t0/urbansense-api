package br.com.urbanintel.infrastructure.adapter.out.persistence;

import br.com.urbanintel.domain.model.Weather;
import br.com.urbanintel.domain.port.out.WeatherRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WeatherPersistenceAdapter implements WeatherRepositoryPort {

    private final WeatherJpaRepository jpaRepository;

    @Override
    public Weather save(Weather weather) {
        return jpaRepository.save(WeatherJpaEntity.fromDomain(weather)).toDomain();
    }

    @Override
    public Optional<Weather> findLatestByCity(String city) {
        return jpaRepository.findTopByCityOrderByRecordedAtDesc(city)
                .map(WeatherJpaEntity::toDomain);
    }

    @Override
    public List<Weather> findHeavyRainSince(double thresholdMm, LocalDateTime since) {
        return jpaRepository.findHeavyRainSince(thresholdMm, since)
                .stream().map(WeatherJpaEntity::toDomain).toList();
    }

    @Override
    public Double avgRainForHour(String city, int hour, LocalDateTime since) {
        return jpaRepository.avgRainForHour(city, hour, since);
    }

    @Override
    public List<Weather> findRainEventsByCity(String city, LocalDateTime since) {
        return jpaRepository.findRainEventsByCity(city, since)
                .stream().map(WeatherJpaEntity::toDomain).toList();
    }
}
