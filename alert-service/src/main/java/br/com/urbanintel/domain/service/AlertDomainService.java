package br.com.urbanintel.domain.service;

import br.com.urbanintel.domain.model.Alert;
import br.com.urbanintel.domain.model.Alert.AlertType;
import br.com.urbanintel.domain.model.Alert.Severity;
import br.com.urbanintel.domain.port.in.AlertQueryUseCase;
import br.com.urbanintel.domain.port.in.EvaluateAlertsUseCase;
import br.com.urbanintel.domain.port.out.AlertPublisherPort;
import br.com.urbanintel.domain.port.out.AlertRepositoryPort;
import br.com.urbanintel.domain.port.out.WeatherDataPort;
import br.com.urbanintel.domain.port.out.WeatherDataPort.WeatherSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertDomainService implements EvaluateAlertsUseCase, AlertQueryUseCase {

    private final AlertRepositoryPort alertRepository;
    private final AlertPublisherPort alertPublisher;
    private final WeatherDataPort weatherDataPort;

    @Override
    @Transactional
    public void evaluate() {
        log.info("Avaliando alertas urbanos...");
        weatherDataPort.findHeavyRainAreas(5.0).forEach(snapshot -> {
            buildAlerts(snapshot).forEach(alert -> {
                Alert saved = alertRepository.save(alert);
                alertPublisher.publish(saved);
                log.info("Alerta gerado: [{}] {} - {}", saved.type(), saved.city(), saved.severity());
            });
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getActiveAlertsByCity(String city) {
        return alertRepository.findActiveByCityOrderBySeverity(city, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAlertsByType(AlertType type) {
        return alertRepository.findByTypeAndActive(type);
    }

    private List<Alert> buildAlerts(WeatherSnapshot w) {
        List<Alert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        if (w.rainMm() != null && w.rainMm() >= 20.0) {
            Severity sev = w.rainMm() >= 50 ? Severity.CRITICAL : Severity.HIGH;
            alerts.add(alert(w, AlertType.FLOODING, sev,
                    String.format("⚠️ Risco de alagamento em %s. Precipitação de %.1fmm/h. " +
                            "Evite vias de baixada e subpassagens.", w.city(), w.rainMm()), now));
        }

        if (w.rainMm() != null && w.rainMm() >= 10.0 && w.rainMm() < 20.0) {
            alerts.add(alert(w, AlertType.HEAVY_RAIN, Severity.MEDIUM,
                    String.format("🌧️ Chuva forte em %s (%.1fmm/h). Leve guarda-chuva.", w.city(), w.rainMm()), now));
        }

        if (w.uvIndex() != null && w.uvIndex() >= 8) {
            Severity sev = w.uvIndex() >= 11 ? Severity.HIGH : Severity.MEDIUM;
            alerts.add(alert(w, AlertType.HIGH_UV, sev,
                    String.format("☀️ UV %d (%s) em %s. Use protetor solar FPS 50+.",
                            w.uvIndex(), uvLabel(w.uvIndex()), w.city()), now));
        }

        int hour = now.getHour();
        boolean isPeak = (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19);
        if (w.rainMm() != null && w.rainMm() >= 5.0 && isPeak) {
            alerts.add(alert(w, AlertType.TRAFFIC_IMPACT, Severity.MEDIUM,
                    String.format("🚦 Chuva + pico em %s. Preveja 40-60%% a mais no deslocamento.", w.city()), now));
        }

        return alerts;
    }

    private Alert alert(WeatherSnapshot w, AlertType type, Severity severity, String message, LocalDateTime now) {
        return new Alert(null, w.city(), type, severity, message, null,
                w.latitude(), w.longitude(), now, now.plusHours(3), true);
    }

    private String uvLabel(int uv) {
        if (uv >= 11) return "Extremo";
        if (uv >= 8)  return "Muito alto";
        return "Alto";
    }
}
