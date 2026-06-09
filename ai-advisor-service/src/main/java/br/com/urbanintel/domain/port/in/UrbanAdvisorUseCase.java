package br.com.urbanintel.domain.port.in;

public interface UrbanAdvisorUseCase {
    String ask(String question, String city);
    DailySummary generateDailySummary(String city);
    String analyzeTransitLine(String lineName, String city);

    record DailySummary(String city, String content, java.time.LocalDateTime generatedAt) {}
}
