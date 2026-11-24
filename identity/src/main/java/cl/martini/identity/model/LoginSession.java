package cl.martini.identity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "login_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginSession {
    @Id @Column(length = 64)
    private String tokenHash;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false)
    private long expiresAt;
}
