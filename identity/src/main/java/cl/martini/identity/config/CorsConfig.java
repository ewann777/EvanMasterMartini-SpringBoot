package cl.martini.identity.config;

import cl.martini.common.HttpCors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource(@Value("${CORS_ORIGINS:}") String origins) {
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", HttpCors.configuration(origins));
        return source;
    }
}
