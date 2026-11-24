package cl.martini.common;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

public final class IdentityClient {
    private final RestClient client;

    public IdentityClient(String identityUrl) {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(3));
        client = RestClient.builder().baseUrl(identityUrl).requestFactory(factory).build();
    }

    public Optional<Account> lookup(String token) {
        try {
            return Optional.ofNullable(client.get().uri("/internal/session")
                    .headers(headers -> headers.setBearerAuth(token)).retrieve().body(Account.class));
        } catch (HttpClientErrorException.Unauthorized error) {
            return Optional.empty();
        } catch (RestClientException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Identidad no disponible", error);
        }
    }
}
