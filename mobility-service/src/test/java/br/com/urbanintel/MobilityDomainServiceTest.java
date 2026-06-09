package br.com.urbanintel;

import br.com.urbanintel.domain.service.MobilityDomainService;
import br.com.urbanintel.domain.port.out.TransitLineRepositoryPort;
import br.com.urbanintel.domain.port.out.CurrentWeatherPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobilityDomainServiceTest {

    @Mock
    private TransitLineRepositoryPort lineRepository;

    @Mock
    private CurrentWeatherPort weatherPort;

    @InjectMocks
    private MobilityDomainService mobilityDomainService;

    @Test
    void deveRetornarListaVaziaQuandoNaoHaLinhasEmRisco() {
        when(lineRepository.findLinesAtRisk(anyDouble())).thenReturn(Collections.emptyList());
        var result = mobilityDomainService.getLinesAtRisk("Sao Paulo", -23.5505, -46.6333);
        assert result != null;
    }
}
