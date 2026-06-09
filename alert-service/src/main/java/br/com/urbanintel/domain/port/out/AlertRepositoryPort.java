package br.com.urbanintel.domain.port.out;

import br.com.urbanintel.domain.model.Alert;
import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepositoryPort {
    Alert save(Alert alert);
    List<Alert> findActiveByCityOrderBySeverity(String city, LocalDateTime now);
    List<Alert> findByTypeAndActive(Alert.AlertType type);
    void deactivateExpired(LocalDateTime now);
}
