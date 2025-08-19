package am.itspace.shortest.url.model;

import am.itspace.shortest.url.model.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "token_tbl")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accessToken;

    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private TokenType type;

    private boolean isExpired;
    private boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
