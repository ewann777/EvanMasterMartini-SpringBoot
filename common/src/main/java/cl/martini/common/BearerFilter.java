package cl.martini.common;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

public final class BearerFilter extends OncePerRequestFilter {
    private static final Pattern HEADER = Pattern.compile("Bearer ([a-f0-9]{64})");
    private final Function<String, Optional<Account>> lookup;

    public BearerFilter(Function<String, Optional<Account>> lookup) {
        this.lookup = lookup;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var matcher = HEADER.matcher(request.getHeader("Authorization") == null ? "" : request.getHeader("Authorization"));
        if (matcher.matches()) {
            try {
                lookup.apply(matcher.group(1)).ifPresent(account -> {
                    var role = new SimpleGrantedAuthority(account.role().equals("admin") ? "ROLE_ADMIN" : "ROLE_USER");
                    var authentication = UsernamePasswordAuthenticationToken.authenticated(account, null, List.of(role));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            } catch (ResponseStatusException error) {
                writeError(response, error.getStatusCode().value(), "Identidad no disponible");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
