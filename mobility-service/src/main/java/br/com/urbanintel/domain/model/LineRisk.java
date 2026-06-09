package br.com.urbanintel.domain.model;

public record LineRisk(
        String code,
        String name,
        String operator,
        String color,
        String vulnerableSection,
        Double vulnerability,
        Integer avgDelayPct,
        int historicalIncidents,
        String riskMessage,
        Double currentRainMm
) {}
