package br.com.urbanintel.infrastructure.adapter.in.messaging;

import br.com.urbanintel.infrastructure.adapter.out.messaging.AlertRabbitPublisher.AlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AlertDlqConsumer {

    @RabbitListener(queues = "urban.alerts.dlq")
    public void handleDlqAlert(AlertEvent event) {
        log.error("[DLQ] Alerta não processado — alertId={}, city={}, type={}, severity={}, publishedAt={}",
                event.alertId(), event.city(), event.type(), event.severity(), event.publishedAt());

    }

    @RabbitListener(queues = "urban.ai.alerts.dlq")
    public void handleAiDlqAlert(AlertEvent event) {
        log.error("[DLQ-AI] Alerta não processado pelo AI Advisor — alertId={}, city={}, severity={}",
                event.alertId(), event.city(), event.severity());
    }
}
