package br.com.urbanintel.infrastructure.adapter.in.web;

import br.com.urbanintel.domain.model.LineRisk;
import br.com.urbanintel.domain.port.in.MobilityQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/mobility")
@RequiredArgsConstructor
@Tag(name = "Mobility", description = "Impacto climático no transporte público urbano")
public class MobilityWebAdapter {

    private final MobilityQueryUseCase mobilityUseCase;

    @GetMapping("/lines-at-risk")
    @Operation(summary = "Linhas em risco agora",
               description = "Cruza chuva atual com histórico de vulnerabilidade de cada linha")
    public ResponseEntity<CollectionModel<EntityModel<LineRisk>>> getLinesAtRisk(
            @RequestParam(defaultValue = "São Paulo") String city,
            @RequestParam(defaultValue = "-23.5505") double lat,
            @RequestParam(defaultValue = "-46.6333") double lon) {

        List<EntityModel<LineRisk>> models = mobilityUseCase.evaluateLinesAtRisk(city, lat, lon)
                .stream()
                .map(risk -> EntityModel.of(risk,
                        linkTo(methodOn(MobilityWebAdapter.class).getLinesAtRisk(city, lat, lon)).withSelfRel()
                )).toList();

        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(MobilityWebAdapter.class).getLinesAtRisk(city, lat, lon)).withSelfRel()));
    }
}
