package br.com.urbanintel.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "weather-service", url = "${services.weather.url:http://localhost:8080}", fallback = WeatherServiceClientFallback.class)
public interface WeatherServiceClient {

    @GetMapping("/api/v1/weather/current")
    WeatherDTO getCurrentWeather(@RequestParam("city") String city, @RequestParam("lat") double lat, @RequestParam("lon") double lon);

    @GetMapping("/api/v1/weather/heavy-rain")
    List<WeatherDTO> getHeavyRainAreas(@RequestParam("thresholdMm") Double thresholdMm);

    @GetMapping("/api/v1/weather/rain-risk")
    RainRiskDTO getRainRisk(@RequestParam("city") String city, @RequestParam("hour") int hour);

    record WeatherDTO(Long id, String city, Double latitude, Double longitude, Double temperature, Double feelsLike, Integer humidity, Double windSpeed, Double rainMm, Integer uvIndex, String condition, String description, LocalDateTime recordedAt) {}

    record RainRiskDTO(String city, int hour, double avgRainMm, int historicalEvents, double riskScore, String level) {}
}
