package am.itspace.shortest.url.dto.request;

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
public class ShortUrlRequest {

  @NotBlank(message = "Original URL is required")
  @Size(max = 2048, message = "Original URL must be at most 2048 characters")
  @URL(message = "Original URL must be a valid URL")
  @Pattern(
      regexp = "^(?i)https?://.+$",
      message = "Original URL must start with http:// or https://"
  )

  private String originalUrl;
}
