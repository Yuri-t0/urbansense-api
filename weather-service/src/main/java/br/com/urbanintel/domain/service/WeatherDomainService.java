package br.com.urbanintel.domain.service;

import br.com.urbanintel.domain.model.RainRisk;
import br.com.urbanintel.domain.model.Weather;
import br.com.urbanintel.domain.port.in.WeatherQueryUseCase;
import br.com.urbanintel.domain.port.out.WeatherApiPort;
import br.com.urbanintel.domain.port.out.WeatherRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherDomainService implements WeatherQueryUseCase {

    private final WeatherApiPort weatherApiPort;
    private final WeatherRepositoryPort weatherRepositoryPort;

    @Override
    public Weather getCurrentWeather(String city, double lat, double lon) {
        log.info("Consultando clima atual para {}", city);
        Weather weather = weatherApiPort.fetchCurrent(city, lat, lon);
        return weatherRepositoryPort.save(weather);
    }

    @Override
    public RainRisk analyzeRainRisk(String city, int hour) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        Double avgRain = weatherRepositoryPort.avgRainForHour(city, hour, since);
        List<Weather> events = weatherRepositoryPort.findRainEventsByCity(city, since);
        return RainRisk.calculate(city, hour, avgRain, events.size());
    }

    @Override
    public List<Weather> getHeavyRainAreas(double thresholdMm) {
        return weatherRepositoryPort.findHeavyRainSince(thresholdMm, LocalDateTime.now().minusHours(1));
    }
}
