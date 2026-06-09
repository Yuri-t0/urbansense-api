package br.com.urbanintel.infrastructure.adapter.in.web;

import br.com.urbanintel.domain.model.RainRisk;
import br.com.urbanintel.domain.model.Weather;
import br.com.urbanintel.domain.port.in.WeatherQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Tag(name = "Weather", description = "Dados climáticos em tempo real")
public class WeatherWebAdapter {

    private final WeatherQueryUseCase weatherUseCase;

    @GetMapping("/current")
    @Operation(summary = "Clima atual de uma cidade")
    public ResponseEntity<EntityModel<WeatherResponse>> getCurrentWeather(
            @RequestParam String city,
            @RequestParam double lat,
            @RequestParam double lon) {

        Weather weather = weatherUseCase.getCurrentWeather(city, lat, lon);
        WeatherResponse response = WeatherResponse.from(weather);

        EntityModel<WeatherResponse> model = EntityModel.of(response,
                linkTo(methodOn(WeatherWebAdapter.class).getCurrentWeather(city, lat, lon)).withSelfRel(),
                linkTo(methodOn(WeatherWebAdapter.class).getRainRisk(city, 17)).withRel("rain-risk"),
                linkTo(methodOn(WeatherWebAdapter.class).getHeavyRain(10.0)).withRel("heavy-rain-areas")
        );
        return ResponseEntity.ok(model);
    }

    @GetMapping("/rain-risk")
    @Operation(summary = "Análise histórica de risco de chuva por hora do dia")
    public ResponseEntity<EntityModel<RainRiskResponse>> getRainRisk(
            @RequestParam String city,
            @RequestParam(defaultValue = "17") int hour) {

        RainRisk risk = weatherUseCase.analyzeRainRisk(city, hour);
        RainRiskResponse response = RainRiskResponse.from(risk);

        EntityModel<RainRiskResponse> model = EntityModel.of(response,
                linkTo(methodOn(WeatherWebAdapter.class).getRainRisk(city, hour)).withSelfRel(),
                linkTo(methodOn(WeatherWebAdapter.class).getCurrentWeather(city, -23.55, -46.63)).withRel("current")
        );
        return ResponseEntity.ok(model);
    }

    @GetMapping("/heavy-rain")
    @Operation(summary = "Regiões com chuva intensa na última hora")
    public ResponseEntity<CollectionModel<EntityModel<WeatherResponse>>> getHeavyRain(
            @RequestParam(defaultValue = "10.0") Double thresholdMm) {

        List<EntityModel<WeatherResponse>> models = weatherUseCase.getHeavyRainAreas(thresholdMm)
                .stream()
                .map(w -> EntityModel.of(WeatherResponse.from(w),
                        linkTo(methodOn(WeatherWebAdapter.class).getHeavyRain(thresholdMm)).withSelfRel()))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(WeatherWebAdapter.class).getHeavyRain(thresholdMm)).withSelfRel()));
    }

    public record WeatherResponse(
            Long id, String city, Double latitude, Double longitude,
            Double temperature, Double feelsLike, Integer humidity,
            Double windSpeed, Double rainMm, Integer uvIndex,
            String condition, String description, LocalDateTime recordedAt
    ) {
        static WeatherResponse from(Weather w) {
            return new WeatherResponse(w.id(), w.city(), w.latitude(), w.longitude(),
                    w.temperature(), w.feelsLike(), w.humidity(), w.windSpeed(),
                    w.rainMm(), w.uvIndex(),
                    w.condition() != null ? w.condition().name() : null,
                    w.description(), w.recordedAt());
        }
    }

    public record RainRiskResponse(
            String city, int hour, double avgRainMm,
            int historicalEvents, double riskScore, String level
    ) {
        static RainRiskResponse from(RainRisk r) {
            return new RainRiskResponse(r.city(), r.hour(), r.avgRainMm(),
                    r.historicalEvents(), r.riskScore(), r.level().name());
        }
    }
}
