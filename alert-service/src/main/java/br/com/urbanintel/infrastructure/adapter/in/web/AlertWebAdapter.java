package br.com.urbanintel.infrastructure.adapter.in.web;

import br.com.urbanintel.domain.model.Alert;
import br.com.urbanintel.domain.port.in.AlertQueryUseCase;
import br.com.urbanintel.domain.port.in.EvaluateAlertsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alertas urbanos em tempo real")
public class AlertWebAdapter {

    private final AlertQueryUseCase alertQuery;
    private final EvaluateAlertsUseCase evaluateAlerts;

    @GetMapping("/city/{city}")
    @Operation(summary = "Alertas ativos de uma cidade ordenados por severidade")
    public ResponseEntity<CollectionModel<EntityModel<AlertResponse>>> getByCity(
            @PathVariable String city) {

        List<EntityModel<AlertResponse>> models = alertQuery.getActiveAlertsByCity(city)
                .stream()
                .map(a -> EntityModel.of(AlertResponse.from(a),
                        linkTo(methodOn(AlertWebAdapter.class).getByCity(city)).withSelfRel(),
                        linkTo(methodOn(AlertWebAdapter.class).getByType(a.type().name())).withRel("by-type")
                )).toList();

        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(AlertWebAdapter.class).getByCity(city)).withSelfRel()));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Alertas ativos por tipo")
    public ResponseEntity<List<AlertResponse>> getByType(@PathVariable String type) {
        Alert.AlertType alertType = Alert.AlertType.valueOf(type.toUpperCase());
        List<AlertResponse> responses = alertQuery.getAlertsByType(alertType)
                .stream().map(AlertResponse::from).toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Força avaliação de alertas (somente ADMIN)")
    public ResponseEntity<String> forceEvaluate() {
        evaluateAlerts.evaluate();
        return ResponseEntity.ok("Avaliação concluída com sucesso.");
    }

    public record AlertResponse(
            Long id, String city, String type, String severity,
            String message, String region, Double lat, Double lon,
            LocalDateTime validFrom, LocalDateTime validUntil, boolean active
    ) {
        static AlertResponse from(Alert a) {
            return new AlertResponse(a.id(), a.city(), a.type().name(), a.severity().name(),
                    a.message(), a.region(), a.lat(), a.lon(),
                    a.validFrom(), a.validUntil(), a.active());
        }
    }
}
