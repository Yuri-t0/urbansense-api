package br.com.urbanintel.infrastructure.adapter.out.external;

import br.com.urbanintel.domain.port.out.WeatherDataPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class WeatherServiceAdapter implements WeatherDataPort {

    @Value("${services.weather.url:http://localhost:8080}")
    private String weatherServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<WeatherSnapshot> findHeavyRainAreas(double thresholdMm) {
        try {
            String url = weatherServiceUrl + "/api/v1/weather/heavy-rain?thresholdMm=" + thresholdMm;
            WeatherListResponse response = restTemplate.getForObject(url, WeatherListResponse.class);
            if (response != null && response.embedded() != null && response.embedded().weatherList() != null) {
                return response.embedded().weatherList().stream()
                        .map(w -> new WeatherSnapshot(w.city(), w.latitude(), w.longitude(), w.temperature(), w.rainMm(), w.uvIndex(), w.condition()))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Weather Service indisponivel. Causa: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record WeatherListResponse(@JsonProperty("_embedded") EmbeddedWrapper embedded) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EmbeddedWrapper(@JsonProperty("weatherResponseList") List<WeatherDto> weatherList) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record WeatherDto(String city, Double latitude, Double longitude, Double temperature, Double feelsLike, Integer humidity, Double windSpeed, Double rainMm, Integer uvIndex, String condition, String description) {}
}
