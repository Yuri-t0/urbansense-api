package br.com.urbanintel.domain.port.in;

import br.com.urbanintel.domain.model.LineRisk;
import java.util.List;

public interface MobilityQueryUseCase {
    List<LineRisk> evaluateLinesAtRisk(String city, double lat, double lon);
}
