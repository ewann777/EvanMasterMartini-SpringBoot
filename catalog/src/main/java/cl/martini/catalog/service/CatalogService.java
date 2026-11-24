package cl.martini.catalog.service;

import static org.springframework.http.HttpStatus.*;

import java.net.URI;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Validator;
import cl.martini.catalog.dto.CatalogDto.*;
import cl.martini.catalog.model.CatalogEntry;
import cl.martini.catalog.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private static final Set<String> VIDEO_HOSTS = Set.of("youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be");

    private final CatalogRepository entries;
    private final ObjectMapper mapper;
    private final Validator validator;

    @Transactional(readOnly = true)
    public List<JsonNode> list(String kind, String query, String type, boolean admin) {
        if (type != null && (!kind.equals("learning") || !Set.of("video", "course").contains(type))) {
            throw new ResponseStatusException(BAD_REQUEST, "Filtro type inválido");
        }
        String[] words = normalize(query).strip().split("\\s+");
        return entries.findByKindOrderBySequenceId(kind).stream()
                .map(CatalogEntry::getPayload)
                .filter(item -> admin || published(item))
                .filter(item -> type == null || item.path("isVideo").asBoolean() == type.equals("video"))
                .filter(item -> {
                    String searchable = normalize(item.toString());
                    return Arrays.stream(words).allMatch(searchable::contains);
                }).toList();
    }

    @Transactional(readOnly = true)
    public JsonNode get(String kind, String id, boolean admin) {
        return entries.findByKindAndSlug(kind, id).map(CatalogEntry::getPayload)
                .filter(item -> admin || published(item)).orElseThrow(CatalogService::missing);
    }

    @Transactional
    public JsonNode create(String kind, ObjectNode input) {
        String id = input.path("id").asText("");
        if (!input.path("id").isTextual() || !id.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new ResponseStatusException(BAD_REQUEST, "ID inválido");
        }
        if (!input.hasNonNull("published")) input.put("published", true);
        if (kind.equals("recipes") && !input.hasNonNull("category")) input.put("category", "General");
        var payload = validated(kind, id, input);
        return entries.saveAndFlush(new CatalogEntry(kind, id, payload)).getPayload();
    }

    @Transactional
    public JsonNode update(String kind, String id, ObjectNode patch) {
        var entry = entries.findForUpdate(kind, id).orElseThrow(CatalogService::missing);
        ObjectNode merged = (ObjectNode) entry.getPayload().deepCopy();
        merged.setAll(patch);
        var payload = validated(kind, id, merged);
        entry.replacePayload(payload);
        return payload;
    }

    @Transactional
    public void delete(String kind, String id) {
        entries.delete(entries.findForUpdate(kind, id).orElseThrow(CatalogService::missing));
    }

    private JsonNode validated(String kind, String id, ObjectNode input) {
        boolean recipe = kind.equals("recipes");
        if ((input.has("published") && !input.path("published").isBoolean())
                || (!recipe && !input.path("isVideo").isBoolean())) {
            throw new ResponseStatusException(BAD_REQUEST, "Los campos published e isVideo deben ser booleanos");
        }
        Object dto;
        try {
            dto = recipe ? mapper.convertValue(input, Recipe.class) : mapper.convertValue(input, Learning.class);
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(BAD_REQUEST, "Campos de catálogo inválidos", error);
        }
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            var violation = violations.iterator().next();
            throw new ResponseStatusException(BAD_REQUEST, violation.getPropertyPath() + ": " + violation.getMessage());
        }
        var payload = (ObjectNode) mapper.valueToTree(dto);
        payload.put("id", id);
        String url = payload.path("videoUrl").asText("").strip();
        if (!url.isEmpty()) {
            validateVideoUrl(url);
            payload.put("videoUrl", url);
        } else if (payload.path("isVideo").asBoolean()) {
            throw new ResponseStatusException(BAD_REQUEST, "El video requiere videoUrl");
        } else if (recipe) {
            payload.put("videoUrl", "");
        } else {
            payload.putNull("videoUrl");
        }
        return payload;
    }

    private static void validateVideoUrl(String value) {
        try {
            URI uri = URI.create(value);
            if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && VIDEO_HOSTS.contains(uri.getHost().toLowerCase(Locale.ROOT)) && uri.getUserInfo() == null) return;
        } catch (IllegalArgumentException ignored) {
            // The client gets the same response for malformed URLs and unsupported hosts.
        }
        throw new ResponseStatusException(BAD_REQUEST, "Ingresa un enlace HTTPS de YouTube");
    }

    private static boolean published(JsonNode item) {
        return item.path("published").asBoolean();
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private static ResponseStatusException missing() {
        return new ResponseStatusException(NOT_FOUND, "Contenido inexistente");
    }
}
