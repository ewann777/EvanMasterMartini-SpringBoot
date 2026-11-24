package cl.martini.contact.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contact_messages")
@Getter
@NoArgsConstructor
public class ContactMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long number;
    @Column(nullable = false, length = 36)
    private String userId;
    @Column(nullable = false, length = 120)
    private String subject;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;
    @Column(nullable = false, length = 12)
    private String status;
    @Column(nullable = false, length = 30)
    private String createdAt;

    public ContactMessage(String userId, String subject, String body, String createdAt) {
        this.userId = userId;
        this.subject = subject;
        this.body = body;
        this.status = "recibida";
        this.createdAt = createdAt;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
