package br.com.urbanintel.domain.port.in;

import br.com.urbanintel.domain.model.Alert;
import java.util.List;

public interface AlertQueryUseCase {
    List<Alert> getActiveAlertsByCity(String city);
    List<Alert> getAlertsByType(Alert.AlertType type);
}
