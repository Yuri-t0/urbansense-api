package br.com.urbanintel.service;

import br.com.urbanintel.domain.model.RainRisk;
import br.com.urbanintel.domain.model.Weather;
import br.com.urbanintel.domain.port.in.WeatherQueryUseCase;
import br.com.urbanintel.dto.WeatherSummaryDTO;
import br.com.urbanintel.model.WeatherRecord;
import br.com.urbanintel.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WeatherQueryUseCase weatherQueryUseCase;
    private final WeatherRepository weatherRepository;

    @Cacheable(value = "weather", key = "#city")
    public WeatherSummaryDTO getCurrentWeather(String city, double lat, double lon) {
        Weather w = weatherQueryUseCase.getCurrentWeather(city, lat, lon);
        return toDTO(w);
    }

    @Transactional(readOnly = true)
    public RainRiskAnalysis analyzeRainRisk(String city, int targetHour) {
        RainRisk risk = weatherQueryUseCase.analyzeRainRisk(city, targetHour);
        return new RainRiskAnalysis(
                risk.city(), risk.hour(), risk.avgRainMm(),
                risk.historicalEvents(), risk.riskScore(), risk.level().name()
        );
    }

    @Cacheable(value = "heavy-rain", key = "'sp'")
    @Transactional(readOnly = true)
    public List<WeatherRecord> getActiveHeavyRain(double thresholdMm) {
        return weatherRepository.findHeavyRainSince(thresholdMm, LocalDateTime.now().minusHours(1));
    }

    @Scheduled(fixedRateString = "${weather.update.interval:900000}")
    @CacheEvict(value = {"weather", "heavy-rain"}, allEntries = true)
    public void refreshMonitoredCities() {
        log.info("Atualizando dados climáticos das cidades monitoradas...");
        MONITORED_CITIES.forEach(c -> getCurrentWeather(c.name(), c.lat(), c.lon()));
    }

    private WeatherSummaryDTO toDTO(Weather w) {
        return new WeatherSummaryDTO(
                w.id(), w.city(), w.latitude(), w.longitude(),
                w.temperature(), w.feelsLike(), w.humidity(),
                w.windSpeed(), w.rainMm(), w.uvIndex(),
                w.condition() != null ? w.condition().name() : null,
                w.description(), w.recordedAt()
        );
    }

    public record RainRiskAnalysis(
            String city, int hour, double avgRainMm,
            int historicalEvents, double riskScore, String level) {}

    private static final List<CityCoord> MONITORED_CITIES = List.of(
            new CityCoord("São Paulo", -23.5505, -46.6333),
            new CityCoord("Guarulhos", -23.4538, -46.5333),
            new CityCoord("Osasco", -23.5322, -46.7919)
    );

    private record CityCoord(String name, double lat, double lon) {}
}
