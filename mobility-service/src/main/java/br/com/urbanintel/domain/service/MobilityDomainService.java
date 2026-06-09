package br.com.urbanintel.domain.service;

import br.com.urbanintel.domain.model.LineRisk;
import br.com.urbanintel.domain.port.in.MobilityQueryUseCase;
import br.com.urbanintel.domain.port.out.CurrentWeatherPort;
import br.com.urbanintel.domain.port.out.TransitLineRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobilityDomainService implements MobilityQueryUseCase {

    private final CurrentWeatherPort currentWeatherPort;
    private final TransitLineRepositoryPort lineRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LineRisk> evaluateLinesAtRisk(String city, double lat, double lon) {
        var weather = currentWeatherPort.fetchCurrent(city, lat, lon);
        double rainMm = weather.rainMm() != null ? weather.rainMm() : 0.0;
        log.info("Avaliando linhas em risco para {} com {}mm de chuva", city, rainMm);

        return lineRepository.findLinesAtRisk(rainMm).stream()
                .map(line -> {
                    int incidents = lineRepository.countRainIncidentsByLine(line.code(), 90);
                    String message = line.buildRiskMessage(rainMm, incidents);
                    return new LineRisk(
                            line.code(), line.name(), line.operator(), line.color(),
                            line.vulnerableSection(), line.rainVulnerability(),
                            line.avgDelayPctOnRain(), incidents, message, rainMm);
                })
                .toList();
    }
}
