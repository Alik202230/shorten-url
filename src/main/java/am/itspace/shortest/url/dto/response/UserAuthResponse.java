package am.itspace.shortest.url.dto.response;

import am.itspace.shortest.url.model.enums.Role;
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
public class UserAuthResponse {
  private String accessToken;
  private String refreshToken;
  private String firstName;
  private String lastName;
  private Long userId;
  private Role role;
  private int statusCode;
}
