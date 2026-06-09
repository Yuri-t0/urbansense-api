package br.com.urbanintel.infrastructure.adapter.out.external;

import br.com.urbanintel.domain.port.out.CurrentWeatherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class WeatherApiAdapter implements CurrentWeatherPort {

    @Value("${services.weather.url:http://localhost:8080}")
    private String weatherServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public WeatherData fetchCurrent(String city, double lat, double lon) {
        try {
            String url = weatherServiceUrl + "/api/v1/weather/current?city={city}&lat={lat}&lon={lon}";
            var response = restTemplate.getForObject(url, WeatherResponse.class, city, lat, lon);
            if (response != null) {
                return new WeatherData(response.city(), response.rainMm(), response.uvIndex(), response.condition());
            }
        } catch (Exception e) {
            log.warn("Weather Service indisponivel para {}. Causa: {}", city, e.getMessage());
        }
        return new WeatherData(city, 0.0, null, "UNKNOWN");
    }

    private record WeatherResponse(String city, Double rainMm, Integer uvIndex, String condition) {}
}
