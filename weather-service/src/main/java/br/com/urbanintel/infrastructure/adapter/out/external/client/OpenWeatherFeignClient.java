package br.com.urbanintel.infrastructure.adapter.out.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "openweather", url = "${weather.api.base-url}")
public interface OpenWeatherFeignClient {

    @GetMapping("/data/2.5/weather")
    OpenWeatherResponse getCurrentWeather(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("appid") String apiKey,
            @RequestParam("units") String units,
            @RequestParam("lang") String lang
    );

    record OpenWeatherResponse(
            MainData main, WindData wind, RainData rain,
            List<WeatherDesc> weather, long dt
    ) {
        public record MainData(double temp, double feels_like, int humidity) {}
        public record WindData(double speed) {}
        public record RainData(Double rain_1h) {}
        public record WeatherDesc(String main, String description) {}
    }
}
