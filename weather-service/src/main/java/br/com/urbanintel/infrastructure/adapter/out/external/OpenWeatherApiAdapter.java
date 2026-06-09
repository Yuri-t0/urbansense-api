package br.com.urbanintel.infrastructure.adapter.out.external;

import br.com.urbanintel.domain.model.Weather;
import br.com.urbanintel.domain.port.out.WeatherApiPort;
import br.com.urbanintel.infrastructure.adapter.out.external.client.OpenWeatherFeignClient;
import br.com.urbanintel.infrastructure.adapter.out.external.client.OpenWeatherFeignClient.OpenWeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenWeatherApiAdapter implements WeatherApiPort {

    private final OpenWeatherFeignClient feignClient;

    @Value("${weather.api.key}")
    private String apiKey;

    @Override
    public Weather fetchCurrent(String city, double lat, double lon) {
        log.info("Chamando OpenWeather para {} ({},{})", city, lat, lon);
        OpenWeatherResponse response = feignClient.getCurrentWeather(lat, lon, apiKey, "metric", "pt_br");
        return toDomain(city, lat, lon, response);
    }

    private Weather toDomain(String city, double lat, double lon, OpenWeatherResponse r) {
        Weather.WeatherCondition condition = mapCondition(
                r.weather() != null && !r.weather().isEmpty() ? r.weather().get(0).main() : "");

        return new Weather(
                null,
                city,
                lat,
                lon,
                r.main().temp(),
                r.main().feels_like(),
                r.main().humidity(),
                r.wind() != null ? r.wind().speed() : 0.0,
                r.rain() != null && r.rain().rain_1h() != null ? r.rain().rain_1h() : 0.0,
                null,
                condition,
                r.weather() != null && !r.weather().isEmpty() ? r.weather().get(0).description() : "",
                LocalDateTime.now()
        );
    }

    private Weather.WeatherCondition mapCondition(String main) {
        return switch (main.toUpperCase()) {
            case "RAIN", "DRIZZLE" -> Weather.WeatherCondition.RAIN;
            case "THUNDERSTORM"    -> Weather.WeatherCondition.STORM;
            case "CLEAR"           -> Weather.WeatherCondition.CLEAR;
            case "CLOUDS"          -> Weather.WeatherCondition.CLOUDY;
            default                -> Weather.WeatherCondition.OTHER;
        };
    }
}
