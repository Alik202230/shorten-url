package am.itspace.shortest.url.model;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "short_url_tbl")
public class ShortUrl {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String shortKey;
  private String originalUrl;
  private Boolean isActive;
  private Integer clickCount;
  private Long userId;
}