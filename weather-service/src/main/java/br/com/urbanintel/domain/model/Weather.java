package br.com.urbanintel.domain.model;

import java.time.LocalDateTime;

public record Weather(
        Long id,
        String city,
        Double latitude,
        Double longitude,
        Double temperature,
        Double feelsLike,
        Integer humidity,
        Double windSpeed,
        Double rainMm,
        Integer uvIndex,
        WeatherCondition condition,
        String description,
        LocalDateTime recordedAt
) {
    public boolean isHeavyRain(double thresholdMm) {
        return rainMm != null && rainMm >= thresholdMm;
    }

    public boolean isStorm() {
        return condition == WeatherCondition.STORM;
    }

    public boolean hasHighUv(int threshold) {
        return uvIndex != null && uvIndex >= threshold;
    }

    public enum WeatherCondition {
        RAIN, STORM, CLEAR, CLOUDY, OTHER, UNKNOWN
    }
}
