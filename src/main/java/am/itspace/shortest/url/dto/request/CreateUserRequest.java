package am.itspace.shortest.url.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  @NotBlank(message = "First name is required")
  @Size(max = 50, message = "First name must be at most 50 characters")
  @Pattern(regexp = "^[\\p{L} .'-]+$", message = "First name contains invalid characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 50, message = "Last name must be at most 50 characters")
  @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Last name contains invalid characters")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 254, message = "Email must be at most 254 characters")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
  @Pattern(
      // at least 1 lowercase, 1 uppercase, 1 digit, 1 special, no spaces
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\p{Punct}])[\\S]{8,72}$",
      message = "Password must contain upper, lower, digit and special character, with no spaces"
  )
  private String password;

  @NotBlank(message = "Original URL is required")
  @Size(max = 2048, message = "Original URL must be at most 2048 characters")
  @URL(message = "Original URL must be a valid URL")
  private String originalUrl;
}
