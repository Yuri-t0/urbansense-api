package br.com.urbanintel.domain.port.out;

import br.com.urbanintel.domain.model.Weather;

public interface WeatherApiPort {
    Weather fetchCurrent(String city, double lat, double lon);
}
