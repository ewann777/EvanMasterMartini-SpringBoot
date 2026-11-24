package cl.martini.identity.config;

import cl.martini.identity.service.IdentityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminConfig {
    @Bean
    ApplicationRunner initialAdmin(IdentityService identity, @Value("${ADMIN_EMAIL:}") String email,
                                   @Value("${ADMIN_PASSWORD:}") String password) {
        return args -> identity.bootstrapAdmin(email, password);
    }
}
