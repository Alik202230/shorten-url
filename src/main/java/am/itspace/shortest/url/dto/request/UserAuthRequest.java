package am.itspace.shortest.url.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UserAuthRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 254, message = "Email must be at most 254 characters")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\p{Punct}])[\\S]{8,72}$",
      message = "Password must contain upper, lower, digit and special character, with no spaces"
  )
  private String password;
}
