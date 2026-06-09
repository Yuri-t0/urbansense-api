package br.com.urbanintel.domain.exception;

public class WeatherNotFoundException extends RuntimeException {
    public WeatherNotFoundException(String city) {
        super("Dados climáticos não encontrados para: " + city);
    }
}
