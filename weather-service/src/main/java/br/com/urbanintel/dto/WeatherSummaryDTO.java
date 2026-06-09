package br.com.urbanintel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.hateoas.RepresentationModel;
import java.time.LocalDateTime;

public record WeatherSummaryDTO(
        Long id,
        String city,
        Double latitude,
        Double longitude,
        Double temperature,
        Double feelsLike,
        Integer humidity,
        Double windSpeed,
        Double rainMm,
        Integer uvIndex,
        String condition,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime recordedAt
) {}
