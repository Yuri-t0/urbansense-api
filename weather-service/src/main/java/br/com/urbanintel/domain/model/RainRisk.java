package br.com.urbanintel.domain.model;

public record RainRisk(
        String city,
        int hour,
        double avgRainMm,
        int historicalEvents,
        double riskScore,
        RiskLevel level
) {
    public enum RiskLevel { LOW, MEDIUM, HIGH }

    public static RainRisk calculate(String city, int hour, Double avgRain, int events) {
        double rainScore = avgRain != null ? Math.min(avgRain / 20.0, 1.0) : 0;
        double freqScore = Math.min(events / 15.0, 1.0);
        double score = (rainScore * 0.6) + (freqScore * 0.4);

        RiskLevel level = score >= 0.7 ? RiskLevel.HIGH
                        : score >= 0.4 ? RiskLevel.MEDIUM
                        : RiskLevel.LOW;

        return new RainRisk(city, hour, avgRain != null ? avgRain : 0.0, events, score, level);
    }
}
