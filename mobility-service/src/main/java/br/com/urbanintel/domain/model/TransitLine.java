package br.com.urbanintel.domain.model;

public record TransitLine(
        Long id,
        String code,
        String name,
        String operator,
        String color,
        Double rainVulnerability,
        Double rainThresholdMm,
        String vulnerableSection,
        Integer avgDelayPctOnRain
) {
    public boolean isAtRisk(double currentRainMm) {
        return rainThresholdMm != null && currentRainMm >= rainThresholdMm;
    }

    public String buildRiskMessage(double rainMm, int historicalIncidents) {
        String msg = String.format("⚠️ %s em risco. Chuva atual: %.1fmm/h (limiar: %.1fmm/h).",
                name, rainMm, rainThresholdMm);
        if (historicalIncidents > 0) {
            msg += String.format(" %d ocorrências similares nos últimos 90 dias. Atrasos médios de %d%%.",
                    historicalIncidents, avgDelayPctOnRain);
        }
        if (vulnerableSection != null) {
            msg += " Trecho mais afetado: " + vulnerableSection + ".";
        }
        return msg;
    }
}
