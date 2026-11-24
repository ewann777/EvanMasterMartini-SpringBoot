package cl.martini.gateway.controller;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class HealthController {
    private final WebClient client;
    private final String identity;
    private final String catalog;
    private final String contact;

    public HealthController(WebClient.Builder builder, @Value("${martini.identity-url}") String identity,
                            @Value("${martini.catalog-url}") String catalog,
                            @Value("${martini.contact-url}") String contact) {
        this.client = builder.build();
        this.identity = identity;
        this.catalog = catalog;
        this.contact = contact;
    }

    @GetMapping("/health")
    Mono<ResponseEntity<Health>> health() {
        return Mono.zip(check(identity), check(catalog), check(contact)).map(checks -> {
            var services = Map.of("identity", checks.getT1(), "catalog", checks.getT2(), "contact", checks.getT3());
            boolean ready = services.values().stream().allMatch(Boolean::booleanValue);
            return ResponseEntity.status(ready ? 200 : 503).body(new Health(ready ? "ok" : "degraded", services));
        });
    }

    private Mono<Boolean> check(String baseUrl) {
        return client.get().uri(baseUrl + "/actuator/health")
                .exchangeToMono(response -> response.releaseBody().thenReturn(response.statusCode().is2xxSuccessful()))
                .timeout(Duration.ofSeconds(3))
                .onErrorReturn(false);
    }

    record Health(String status, Map<String, Boolean> services) {}
}
