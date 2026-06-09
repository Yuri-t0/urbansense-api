package br.com.urbanintel.domain.port.out;

import java.util.List;

public interface WeatherDataPort {

    List<WeatherSnapshot> findHeavyRainAreas(double thresholdMm);

    record WeatherSnapshot(
            String city, Double latitude, Double longitude,
            Double temperature, Double rainMm, Integer uvIndex,
            String condition
    ) {}
}
