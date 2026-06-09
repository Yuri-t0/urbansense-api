package br.com.urbanintel;

import br.com.urbanintel.domain.model.RainRisk;
import br.com.urbanintel.domain.model.Weather;
import br.com.urbanintel.domain.port.in.WeatherQueryUseCase;
import br.com.urbanintel.model.WeatherRecord;
import br.com.urbanintel.repository.WeatherRepository;
import br.com.urbanintel.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService - Testes Unitários")
class WeatherServiceTest {

    @Mock
    private WeatherQueryUseCase weatherQueryUseCase;

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    @DisplayName("Deve retornar clima atual delegando ao use case")
    void getCurrentWeather_shouldDelegateToUseCase() {
        var weather = new Weather(1L, "São Paulo", -23.55, -46.63,
                28.5, 30.0, 65, 3.5, 2.0, null,
                Weather.WeatherCondition.RAIN, "chuva fraca", LocalDateTime.now());

        when(weatherQueryUseCase.getCurrentWeather("São Paulo", -23.55, -46.63))
                .thenReturn(weather);

        var result = weatherService.getCurrentWeather("São Paulo", -23.55, -46.63);

        assertThat(result).isNotNull();
        assertThat(result.city()).isEqualTo("São Paulo");
        assertThat(result.temperature()).isEqualTo(28.5);
        verify(weatherQueryUseCase, times(1)).getCurrentWeather("São Paulo", -23.55, -46.63);
    }

    @Test
    @DisplayName("Deve retornar análise de risco HIGH delegando ao use case")
    void analyzeRainRisk_shouldReturnHighRisk() {
        var risk = RainRisk.calculate("São Paulo", 17, 18.5, 12);

        when(weatherQueryUseCase.analyzeRainRisk("São Paulo", 17)).thenReturn(risk);

        var result = weatherService.analyzeRainRisk("São Paulo", 17);

        assertThat(result.level()).isEqualTo("HIGH");
        assertThat(result.riskScore()).isGreaterThanOrEqualTo(0.7);
    }

    @Test
    @DisplayName("Deve retornar risco LOW quando sem histórico")
    void analyzeRainRisk_shouldReturnLowRisk() {
        var risk = RainRisk.calculate("São Paulo", 10, 0.5, 0);

        when(weatherQueryUseCase.analyzeRainRisk("São Paulo", 10)).thenReturn(risk);

        var result = weatherService.analyzeRainRisk("São Paulo", 10);

        assertThat(result.level()).isEqualTo("LOW");
        assertThat(result.riskScore()).isLessThan(0.4);
    }

    @Test
    @DisplayName("Deve buscar áreas com chuva forte no repositório")
    void getActiveHeavyRain_shouldQueryRepository() {
        var record = WeatherRecord.builder()
                .city("Guarulhos").rainMm(25.0).condition("STORM")
                .recordedAt(LocalDateTime.now()).build();

        when(weatherRepository.findHeavyRainSince(eq(10.0), any(LocalDateTime.class)))
                .thenReturn(List.of(record));

        var result = weatherService.getActiveHeavyRain(10.0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Guarulhos");
    }

    @Test
    @DisplayName("Condição STORM deve ser mapeada corretamente no DTO")
    void getCurrentWeather_shouldMapStormCondition() {
        var weather = new Weather(2L, "São Paulo", -23.55, -46.63,
                22.0, 21.0, 90, 12.0, 35.0, null,
                Weather.WeatherCondition.STORM, "trovoada", LocalDateTime.now());

        when(weatherQueryUseCase.getCurrentWeather(anyString(), anyDouble(), anyDouble()))
                .thenReturn(weather);

        var result = weatherService.getCurrentWeather("São Paulo", -23.55, -46.63);

        assertThat(result.condition()).isEqualTo("STORM");
    }
}
