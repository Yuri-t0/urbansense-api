package br.com.urbanintel.controller;

import br.com.urbanintel.domain.model.RainRisk;
import br.com.urbanintel.domain.model.Weather;
import br.com.urbanintel.domain.port.in.WeatherQueryUseCase;
import br.com.urbanintel.model.WeatherRecord;
import br.com.urbanintel.security.JwtAuthenticationFilter;
import br.com.urbanintel.security.JwtTokenProvider;
import br.com.urbanintel.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
@DisplayName("WeatherController - Testes de Integração (MockMvc)")
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @MockBean
    private WeatherQueryUseCase weatherQueryUseCase;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /weather/current deve retornar 200 com dados climáticos")
    void getCurrentWeather_shouldReturn200() throws Exception {
        var dto = new br.com.urbanintel.dto.WeatherSummaryDTO(
                1L, "São Paulo", -23.55, -46.63,
                28.0, 30.0, 70, 3.5, 5.0, 7,
                "RAIN", "chuva fraca", LocalDateTime.now()
        );

        when(weatherService.getCurrentWeather(eq("São Paulo"), anyDouble(), anyDouble()))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/weather/current")
                        .param("city", "São Paulo")
                        .param("lat", "-23.55")
                        .param("lon", "-46.63")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("São Paulo"))
                .andExpect(jsonPath("$.temperature").value(28.0))
                .andExpect(jsonPath("$.condition").value("RAIN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /weather/rain-risk deve retornar análise com score")
    void getRainRisk_shouldReturnAnalysis() throws Exception {
        var analysis = new WeatherService.RainRiskAnalysis(
                "São Paulo", 17, 12.5, 8, 0.75, "HIGH"
        );

        when(weatherService.analyzeRainRisk(eq("São Paulo"), eq(17))).thenReturn(analysis);

        mockMvc.perform(get("/api/v1/weather/rain-risk")
                        .param("city", "São Paulo")
                        .param("hour", "17")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("HIGH"))
                .andExpect(jsonPath("$.riskScore").value(0.75));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /weather/heavy-rain deve retornar lista vazia com 200")
    void getHeavyRain_shouldReturn200() throws Exception {
        when(weatherService.getActiveHeavyRain(anyDouble())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/weather/heavy-rain")
                        .param("thresholdMm", "10.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /weather/current com usuário autenticado deve retornar 200")
    void getCurrentWeather_withAuth_shouldReturn200() throws Exception {
        var dto = new br.com.urbanintel.dto.WeatherSummaryDTO(
                1L, "São Paulo", -23.55, -46.63,
                25.0, 26.0, 80, 2.0, 0.0, 5,
                "CLEAR", "céu limpo", LocalDateTime.now()
        );

        when(weatherService.getCurrentWeather(anyString(), anyDouble(), anyDouble()))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/weather/current")
                        .param("city", "São Paulo")
                        .param("lat", "-23.55")
                        .param("lon", "-46.63")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
