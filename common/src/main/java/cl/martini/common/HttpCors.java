package cl.martini.common;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.cors.CorsConfiguration;

public final class HttpCors {
    private HttpCors() {}

    public static CorsConfiguration configuration(String origins) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(600L);
        return config;
    }
}
