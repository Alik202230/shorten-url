package am.itspace.shortest.url.model;

import am.itspace.shortest.url.model.enums.TokenType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
