package br.com.urbanintel.domain.port.out;

public interface CurrentWeatherPort {
    WeatherData fetchCurrent(String city, double lat, double lon);

    record WeatherData(String city, Double rainMm, Integer uvIndex, String condition) {}
}
