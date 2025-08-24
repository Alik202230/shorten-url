package am.itspace.shortest.url.dto.error;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

  private String message;
  private int statusCode;
  private HttpStatus status;
  private LocalDateTime timestamp;
}
