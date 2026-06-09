package br.com.urbanintel.domain.port.in;

import br.com.urbanintel.domain.model.RainRisk;
import br.com.urbanintel.domain.model.Weather;

import java.util.List;

public interface WeatherQueryUseCase {
    Weather getCurrentWeather(String city, double lat, double lon);
    RainRisk analyzeRainRisk(String city, int hour);
    List<Weather> getHeavyRainAreas(double thresholdMm);
}
