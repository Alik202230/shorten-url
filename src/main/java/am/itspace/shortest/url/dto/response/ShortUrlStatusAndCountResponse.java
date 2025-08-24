package am.itspace.shortest.url.dto.response;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlStatusAndCountResponse {
  private String shortKey;
  private Boolean isActive;
  private Integer clickCount;
}
