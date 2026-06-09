package br.com.urbanintel;

import br.com.urbanintel.domain.service.AlertDomainService;
import br.com.urbanintel.domain.port.out.AlertRepositoryPort;
import br.com.urbanintel.domain.port.out.AlertPublisherPort;
import br.com.urbanintel.domain.port.out.WeatherDataPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertDomainServiceTest {

    @Mock
    private AlertRepositoryPort alertRepository;

    @Mock
    private AlertPublisherPort alertPublisher;

    @Mock
    private WeatherDataPort weatherData;

    @InjectMocks
    private AlertDomainService alertDomainService;

    @Test
    void deveRetornarListaVaziaQuandoNaoHaChuva() {
        when(weatherData.findHeavyRainAreas(anyDouble())).thenReturn(Collections.emptyList());
        alertDomainService.evaluateAlerts();
        verify(alertPublisher, never()).publish(any());
    }

    @Test
    void deveRetornarAlertasAtivosPorCidade() {
        when(alertRepository.findActiveByCityOrderBySeverity(anyString())).thenReturn(Collections.emptyList());
        var result = alertDomainService.getActiveAlertsByCity("Sao Paulo");
        assert result != null;
    }
}
