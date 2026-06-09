package br.com.urbanintel.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "alert-service", url = "${services.alert.url:http://localhost:8081}", fallback = AlertServiceClientFallback.class)
public interface AlertServiceClient {

    @GetMapping("/api/v1/alerts/city/{city}")
    List<AlertDTO> getAlertsByCity(@PathVariable("city") String city);

    @GetMapping("/api/v1/alerts/type/{type}")
    List<AlertDTO> getAlertsByType(@RequestParam("type") String type);

    record AlertDTO(Long id, String city, String type, String severity, String message, String region, boolean active) {}
}
