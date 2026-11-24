package cl.martini.identity.service;

import static org.springframework.http.HttpStatus.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import cl.martini.common.Account;
import cl.martini.identity.dto.IdentityDto.*;
import cl.martini.identity.model.LoginSession;
import cl.martini.identity.model.User;
import cl.martini.identity.repository.SessionRepository;
import cl.martini.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IdentityService {
    private static final long SESSION_SECONDS = Duration.ofHours(8).toSeconds();
    private final UserRepository users;
    private final SessionRepository sessions;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();
    private final String dummyHash;

    public IdentityService(UserRepository users, SessionRepository sessions, PasswordEncoder encoder) {
        this.users = users;
        this.sessions = sessions;
        this.encoder = encoder;
        this.dummyHash = encoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public Profile register(Register request) {
        return create(request.details(), request.password(), "user");
    }

    @Transactional
    public void bootstrapAdmin(String email, String password) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 8 || password.length() > 128) {
            throw new IllegalStateException("Configura ADMIN_EMAIL y ADMIN_PASSWORD (8 a 128 caracteres)");
        }
        var existing = users.findByEmail(normalizeEmail(email));
        if (existing.isPresent()) {
            if (!existing.get().getRole().equals("admin")) throw new IllegalStateException("Correo de administrador ya ocupado");
            return;
        }
        create(new ProfilePatch("Administrador", email, "admin", null, null, null, null, null, null, null),
                password, "admin");
    }

    @Transactional
    public Session login(Login request) {
        User user = users.findByEmail(normalizeEmail(request.correo())).orElse(null);
        boolean valid = encoder.matches(request.password(), user == null ? dummyHash : user.getPasswordHash());
        if (!valid || user == null) throw new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas");
        byte[] secret = new byte[32];
        random.nextBytes(secret);
        String token = HexFormat.of().formatHex(secret);
        long now = System.currentTimeMillis();
        sessions.deleteExpired(now);
        sessions.save(new LoginSession(hash(token), user, now + SESSION_SECONDS * 1000));
        return new Session(token, SESSION_SECONDS, profile(user));
    }

    @Transactional(readOnly = true)
    public Optional<Account> lookup(String token) {
        return sessions.findByTokenHashAndExpiresAtGreaterThan(hash(token), System.currentTimeMillis())
                .map(session -> new Account(session.getUser().getId(), session.getUser().getRole()));
    }

    @Transactional
    public void logout(String token) {
        sessions.revoke(hash(token));
    }

    @Transactional(readOnly = true)
    public Profile getProfile(String id) {
        return users.findById(id).map(IdentityService::profile)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Perfil inexistente"));
    }

    @Transactional
    public Profile updateProfile(String id, ProfilePatch patch) {
        User user = users.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Perfil inexistente"));
        apply(user, patch);
        users.flush();
        return profile(user);
    }

    private Profile create(ProfilePatch details, String password, String role) {
        var user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setRole(role);
        user.setPasswordHash(encoder.encode(password));
        apply(user, details);
        return profile(users.saveAndFlush(user));
    }

    private static void apply(User user, ProfilePatch patch) {
        if (patch.nombre() != null) user.setNombre(required(patch.nombre(), "nombre", 1));
        if (patch.correo() != null) user.setEmail(normalizeEmail(patch.correo()));
        if (patch.usuario() != null) user.setUsername(required(patch.usuario(), "usuario", 3));
        if (patch.nombreNegocio() != null) user.setNombreNegocio(patch.nombreNegocio().strip());
        if (patch.tipoNegocio() != null) user.setTipoNegocio(patch.tipoNegocio().strip());
        if (patch.empleados() != null) user.setEmpleados(patch.empleados().strip());
        if (patch.telefono() != null) user.setTelefono(patch.telefono().strip());
        if (patch.direccion() != null) user.setDireccion(patch.direccion().strip());
        if (patch.rutEmpresa() != null) user.setRutEmpresa(patch.rutEmpresa().strip());
        if (patch.intereses() != null) user.setIntereses(patch.intereses().stream().map(String::strip).distinct().toList());
    }

    private static String normalizeEmail(String email) {
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || normalized.length() > 254) {
            throw new ResponseStatusException(BAD_REQUEST, "Correo inválido");
        }
        return normalized;
    }

    private static String required(String value, String field, int min) {
        String stripped = value.strip();
        if (stripped.length() < min) throw new ResponseStatusException(BAD_REQUEST, field + " inválido");
        return stripped;
    }

    private static Profile profile(User user) {
        return new Profile(user.getId(), user.getRole(), user.getNombre(), user.getEmail(), user.getUsername(),
                user.getNombreNegocio(), user.getTipoNegocio(), user.getEmpleados(), user.getTelefono(),
                user.getDireccion(), user.getRutEmpresa(), user.getIntereses());
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }
}
