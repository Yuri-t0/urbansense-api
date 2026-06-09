package br.com.urbanintel.domain.port.out;

import br.com.urbanintel.domain.model.Alert;

public interface AlertPublisherPort {
    void publish(Alert alert);
}
