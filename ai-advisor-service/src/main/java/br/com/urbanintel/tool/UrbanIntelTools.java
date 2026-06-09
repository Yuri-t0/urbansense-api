package br.com.urbanintel.tool;

import br.com.urbanintel.client.AlertServiceClient;
import br.com.urbanintel.client.WeatherServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UrbanIntelTools {

    private final WeatherServiceClient weatherClient;
    private final AlertServiceClient alertClient;

    @Tool(description = """
        Busca as condições climáticas atuais de uma cidade brasileira.
        Retorna temperatura, chuva (mm/h), umidade, vento e condição geral.
        Use para responder perguntas sobre o clima do momento.
        """)
    public String getCurrentWeather(
            @ToolParam(description = "Nome da cidade, ex: 'São Paulo'") String city,
            @ToolParam(description = "Latitude da cidade") double latitude,
            @ToolParam(description = "Longitude da cidade") double longitude) {

        log.info("[Tool] getCurrentWeather chamado para {}", city);
        try {
            var weather = weatherClient.getCurrentWeather(city, latitude, longitude);
            return """
                Cidade: %s
                Temperatura: %.1f°C (sensação: %.1f°C)
                Chuva: %.1f mm/h
                Umidade: %d%%
                Vento: %.1f km/h
                Condição: %s - %s
                Horário: %s
                """.formatted(
                    weather.city(), weather.temperature(), weather.feelsLike(),
                    weather.rainMm() != null ? weather.rainMm() : 0.0,
                    weather.humidity(), weather.windSpeed() * 3.6,
                    weather.condition(), weather.description(),
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("[Tool] Erro ao buscar clima: {}", e.getMessage());
            return "Dados climáticos temporariamente indisponíveis para " + city;
        }
    }

    @Tool(description = """
        Retorna todos os alertas urbanos ativos para uma cidade.
        Inclui alertas de alagamento, chuva forte, UV alto, atrasos no transporte.
        Cada alerta tem severidade (LOW/MEDIUM/HIGH/CRITICAL) e mensagem detalhada.
        Use quando o usuário perguntar sobre riscos, alertas ou condições especiais.
        """)
    public String getActiveAlerts(
            @ToolParam(description = "Nome da cidade") String city) {

        log.info("[Tool] getActiveAlerts chamado para {}", city);
        try {
            var alerts = alertClient.getAlertsByCity(city);
            if (alerts.isEmpty()) {
                return "Nenhum alerta ativo para " + city + " no momento.";
            }
            StringBuilder sb = new StringBuilder("Alertas ativos em " + city + ":\n");
            alerts.forEach(a -> sb.append(String.format(
                    "[%s] %s: %s\n", a.severity(), a.type(), a.message())));
            return sb.toString();
        } catch (Exception e) {
            log.error("[Tool] Erro ao buscar alertas: {}", e.getMessage());
            return "Sistema de alertas temporariamente indisponível.";
        }
    }

    @Tool(description = """
        Analisa o risco histórico de chuva para uma cidade em uma hora específica do dia.
        Retorna score de risco (0.0-1.0) e nível (LOW/MEDIUM/HIGH) baseado nos últimos 30 dias.
        Use para responder perguntas como 'vai chover amanhã de tarde?' ou
        'qual o risco de chuva na hora do rush?'
        """)
    public String getRainRiskForHour(
            @ToolParam(description = "Nome da cidade") String city,
            @ToolParam(description = "Hora do dia (0-23)") int hour) {

        log.info("[Tool] getRainRiskForHour: {} às {}h", city, hour);
        try {
            var risk = weatherClient.getRainRisk(city, hour);
            return """
                Análise de risco de chuva para %s às %dh:
                - Score de risco: %.0f%%
                - Nível: %s
                - Média histórica de chuva nesse horário: %.1f mm
                - Eventos de chuva nos últimos 30 dias nesse horário: %d
                """.formatted(
                    city, hour,
                    risk.riskScore() * 100, risk.level(),
                    risk.avgRainMm(), risk.historicalEvents()
            );
        } catch (Exception e) {
            return "Análise de risco indisponível para " + city + " às " + hour + "h.";
        }
    }

    @Tool(description = """
        Retorna informações sobre linhas de metrô/trem de São Paulo que têm histórico
        de impacto em dias de chuva. Útil para alertas de mobilidade urbana.
        """)
    public String getMetroLinesRainImpact() {
        
        return """
            Histórico de impacto de chuva nas linhas de metrô/trem de SP:
            
            - Linha 3 Vermelha: ALTO RISCO em chuvas > 15mm. Trecho Brás-Corinthians frequentemente afetado.
            - Linha 10 Turquesa (CPTM): CRÍTICO em chuvas > 20mm. Alagamentos no trecho ABC.
            - Linha 11 Coral (CPTM): ALTO em chuvas > 15mm. Zona Leste historicamente impactada.
            - Linha 8 Diamante (CPTM): MÉDIO em chuvas > 10mm. Trecho Lapa-Amador Aguiar.
            - Linha 4 Amarela: BAIXO RISCO (infraestrutura moderna e subterrânea).
            - Linha 5 Lilás: BAIXO RISCO (totalmente subterrânea).
            
            Durante horários de pico (7h-9h e 17h-19h), o impacto é potencializado.
            """;
    }
}
