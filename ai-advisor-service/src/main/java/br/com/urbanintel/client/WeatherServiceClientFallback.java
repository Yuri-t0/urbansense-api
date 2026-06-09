package br.com.urbanintel.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class WeatherServiceClientFallback implements WeatherServiceClient {

    @Override
    public WeatherDTO getCurrentWeather(String city, double lat, double lon) {
        log.warn("Weather Service indisponível. Fallback para getCurrentWeather({}).", city);
        return new WeatherDTO(null, city, lat, lon, null, null, null, null, 0.0, null,
                "UNKNOWN", "Dados indisponíveis", null);
    }

    @Override
    public List<WeatherDTO> getHeavyRainAreas(Double thresholdMm) {
        log.warn("Weather Service indisponível. Fallback para getHeavyRainAreas.");
        return Collections.emptyList();
    }

    @Override
    public RainRiskDTO getRainRisk(String city, int hour) {
        log.warn("Weather Service indisponível. Fallback para getRainRisk({}, {}).", city, hour);
        return new RainRiskDTO(city, hour, 0.0, 0, 0.0, "UNKNOWN");
    }
}
