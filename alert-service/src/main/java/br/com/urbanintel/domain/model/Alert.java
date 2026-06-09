package br.com.urbanintel.domain.model;

import java.time.LocalDateTime;

public record Alert(
        Long id,
        String city,
        AlertType type,
        Severity severity,
        String message,
        String region,
        Double lat,
        Double lon,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        boolean active
) {
    public enum AlertType {
        FLOODING, HEAVY_RAIN, HIGH_UV, TRANSIT_DELAY, TRAFFIC_IMPACT, HEAT_WAVE, STRONG_WIND
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }
}
