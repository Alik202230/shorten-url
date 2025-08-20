package am.itspace.shortest.url.dto.response;

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
public class RefreshTokenResponse {
  private String accessToken;
  private String refreshToken;
  private int statusCode;
}
