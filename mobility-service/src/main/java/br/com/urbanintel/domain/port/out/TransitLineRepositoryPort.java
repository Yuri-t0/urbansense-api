package br.com.urbanintel.domain.port.out;

import br.com.urbanintel.domain.model.TransitLine;
import java.util.List;

public interface TransitLineRepositoryPort {
    List<TransitLine> findLinesAtRisk(double rainMm);
    List<TransitLine> findAll();
    int countRainIncidentsByLine(String lineCode, int days);
}
