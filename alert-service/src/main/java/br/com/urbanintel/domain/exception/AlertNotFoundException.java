package br.com.urbanintel.domain.exception;

public class AlertNotFoundException extends RuntimeException {
    public AlertNotFoundException(Long id) {
        super("Alerta não encontrado: " + id);
    }
}

