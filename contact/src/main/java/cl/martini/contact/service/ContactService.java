package cl.martini.contact.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import cl.martini.contact.dto.ContactDto.*;
import cl.martini.contact.model.ContactMessage;
import cl.martini.contact.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository messages;

    @Transactional
    public Message create(String userId, Create input) {
        String subject = input.subject().strip();
        String body = input.body().strip();
        if (subject.length() < 3 || body.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asunto o texto demasiado breve");
        }
        var message = new ContactMessage(userId, subject, body, Instant.now().truncatedTo(ChronoUnit.MILLIS).toString());
        return view(messages.save(message));
    }

    @Transactional(readOnly = true)
    public List<Message> inbox(String userId) {
        return messages.findByUserIdOrderByNumberDesc(userId).stream().map(ContactService::view).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminMessage> adminInbox() {
        return messages.findAllByOrderByNumberDesc().stream()
                .map(m -> new AdminMessage(m.getNumber(), m.getSubject(), m.getBody(), m.getStatus(), m.getCreatedAt(), m.getUserId()))
                .toList();
    }

    @Transactional
    public Message changeStatus(long number, Status input) {
        var message = messages.findForUpdate(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta inexistente"));
        message.changeStatus(input.status());
        return view(message);
    }

    private static Message view(ContactMessage message) {
        return new Message(message.getNumber(), message.getSubject(), message.getBody(), message.getStatus(), message.getCreatedAt());
    }
}
