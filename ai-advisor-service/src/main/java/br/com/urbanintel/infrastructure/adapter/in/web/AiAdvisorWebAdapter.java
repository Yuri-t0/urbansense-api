package br.com.urbanintel.infrastructure.adapter.in.web;

import br.com.urbanintel.domain.port.in.UrbanAdvisorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/advisor")
@RequiredArgsConstructor
@Tag(name = "AI Advisor", description = "Assistente urbano inteligente com Spring AI")
public class AiAdvisorWebAdapter {

    private final UrbanAdvisorUseCase advisorUseCase;

    @PostMapping("/ask")
    @Operation(summary = "Consulta ao assistente urbano com RAG + Tooling")
    public ResponseEntity<EntityModel<AdvisorResponse>> ask(
            @Valid @RequestBody AskRequest request) {

        String answer = advisorUseCase.ask(request.question(), request.city());
        AdvisorResponse response = new AdvisorResponse(request.question(), answer);

        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(AiAdvisorWebAdapter.class).ask(request)).withSelfRel(),
                linkTo(methodOn(AiAdvisorWebAdapter.class).getDailySummary(request.city())).withRel("daily-summary")
        ));
    }

    @GetMapping("/daily-summary/{city}")
    @Operation(summary = "Resumo diário gerado por IA com score do dia")
    public ResponseEntity<UrbanAdvisorUseCase.DailySummary> getDailySummary(
            @PathVariable String city) {
        return ResponseEntity.ok(advisorUseCase.generateDailySummary(city));
    }

    @GetMapping("/transit/{lineName}")
    @Operation(summary = "Análise de risco climático para uma linha de transporte")
    public ResponseEntity<String> analyzeTransitLine(
            @PathVariable String lineName,
            @RequestParam(defaultValue = "São Paulo") String city) {
        return ResponseEntity.ok(advisorUseCase.analyzeTransitLine(lineName, city));
    }

    public record AskRequest(
            @NotBlank(message = "A pergunta não pode estar vazia") String question,
            @NotBlank(message = "A cidade não pode estar vazia") String city
    ) {}

    public record AdvisorResponse(String question, String answer) {}
}
