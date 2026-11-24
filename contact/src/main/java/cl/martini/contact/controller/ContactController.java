package cl.martini.contact.controller;

import java.util.List;
import jakarta.validation.Valid;
import cl.martini.common.Account;
import cl.martini.contact.dto.ContactDto.*;
import cl.martini.contact.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contact;

    @GetMapping("/contact")
    List<Message> inbox(@AuthenticationPrincipal Account account) {
        return contact.inbox(account.id());
    }

    @PostMapping("/contact")
    @ResponseStatus(HttpStatus.CREATED)
    Message create(@AuthenticationPrincipal Account account, @Valid @RequestBody Create input) {
        return contact.create(account.id(), input);
    }

    @GetMapping("/admin/contact")
    List<AdminMessage> adminInbox() {
        return contact.adminInbox();
    }

    @PatchMapping("/admin/contact/{number}")
    Message changeStatus(@PathVariable long number, @Valid @RequestBody Status input) {
        return contact.changeStatus(number, input);
    }
}
