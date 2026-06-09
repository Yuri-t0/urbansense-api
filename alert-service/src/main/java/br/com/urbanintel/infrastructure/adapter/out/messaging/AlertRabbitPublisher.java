package br.com.urbanintel.infrastructure.adapter.out.messaging;

import br.com.urbanintel.domain.model.Alert;
import br.com.urbanintel.domain.port.out.AlertPublisherPort;
import br.com.urbanintel.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertRabbitPublisher implements AlertPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(Alert alert) {
        String routingKey = buildRoutingKey(alert);
        AlertEvent event = AlertEvent.from(alert);

        log.info("Publicando alerta [{}] routing={}", alert.id(), routingKey);
        rabbitTemplate.convertAndSend(RabbitMQConfig.ALERTS_EXCHANGE, routingKey, event);
    }

    private String buildRoutingKey(Alert alert) {
        String city = normalizeCity(alert.city());
        return "alert." + alert.type().name().toLowerCase() + "." + city;
    }

    private String normalizeCity(String city) {
        return city.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .replace("saopaulo", "sp")
                .replace("riodejaneiro", "rj");
    }

    public record AlertEvent(
            Long alertId, String city, String type, String severity,
            String message, String region, Double lat, Double lon,
            LocalDateTime validFrom, LocalDateTime validUntil,
            LocalDateTime publishedAt
    ) {
        static AlertEvent from(Alert a) {
            return new AlertEvent(a.id(), a.city(), a.type().name(), a.severity().name(),
                    a.message(), a.region(), a.lat(), a.lon(),
                    a.validFrom(), a.validUntil(), LocalDateTime.now());
        }
    }
}
