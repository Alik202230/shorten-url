package am.itspace.shortest.url.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
