package cl.martini.catalog.controller;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cl.martini.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalog;

    @GetMapping("/{kind:recipes|learning}")
    List<JsonNode> list(@PathVariable String kind, @RequestParam(defaultValue = "") String q,
                        @RequestParam(required = false) String type) {
        return catalog.list(kind, q, type, false);
    }

    @GetMapping("/{kind:recipes|learning}/{id}")
    JsonNode detail(@PathVariable String kind, @PathVariable String id) {
        return catalog.get(kind, id, false);
    }

    @GetMapping("/admin/{kind:recipes|learning}")
    List<JsonNode> listAdmin(@PathVariable String kind, @RequestParam(defaultValue = "") String q,
                             @RequestParam(required = false) String type) {
        return catalog.list(kind, q, type, true);
    }

    @GetMapping("/admin/{kind:recipes|learning}/{id}")
    JsonNode detailAdmin(@PathVariable String kind, @PathVariable String id) {
        return catalog.get(kind, id, true);
    }

    @PostMapping("/admin/{kind:recipes|learning}")
    @ResponseStatus(HttpStatus.CREATED)
    JsonNode create(@PathVariable String kind, @RequestBody ObjectNode body) {
        return catalog.create(kind, body);
    }

    @PatchMapping("/admin/{kind:recipes|learning}/{id}")
    JsonNode update(@PathVariable String kind, @PathVariable String id, @RequestBody ObjectNode body) {
        return catalog.update(kind, id, body);
    }

    @DeleteMapping("/admin/{kind:recipes|learning}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable String kind, @PathVariable String id) {
        catalog.delete(kind, id);
    }
}
