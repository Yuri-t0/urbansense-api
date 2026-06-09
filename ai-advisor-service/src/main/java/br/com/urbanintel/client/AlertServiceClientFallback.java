package br.com.urbanintel.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class AlertServiceClientFallback implements AlertServiceClient {

    @Override
    public List<AlertDTO> getAlertsByCity(String city) {
        log.warn("Alert Service indisponível. Fallback ativado para getAlertsByCity({}).", city);
        return Collections.emptyList();
    }

    @Override
    public List<AlertDTO> getAlertsByType(String type) {
        log.warn("Alert Service indisponível. Fallback ativado para getAlertsByType({}).", type);
        return Collections.emptyList();
    }
}
