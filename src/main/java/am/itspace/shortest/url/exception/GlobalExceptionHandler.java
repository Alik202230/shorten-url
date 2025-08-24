package am.itspace.shortest.url.exception;

import am.itspace.shortest.url.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  @ExceptionHandler(CompromisedPasswordException.class)
  public ProblemDetail handleCompromisedPasswordException(CompromisedPasswordException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler({
      UserNotFoundException.class,
      UserAlreadyExistsException.class,
      EmailOrPasswordException.class,
  })
  public ResponseEntity<ErrorResponse> handleException(Exception exception) {
    return switch (exception) {
      case UserNotFoundException ex -> {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .statusCode(HttpStatus.NOT_FOUND.value())
            .status(HttpStatus.NOT_FOUND)
            .timestamp(LocalDateTime.now())
            .build();
        yield new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
      }
      case UserAlreadyExistsException ex -> {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .statusCode(HttpStatus.NOT_FOUND.value())
            .status(HttpStatus.NOT_FOUND)
            .timestamp(LocalDateTime.now())
            .build();
        yield new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
      }
      case EmailOrPasswordException ex -> {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .statusCode(HttpStatus.NOT_FOUND.value())
            .status(HttpStatus.NOT_FOUND)
            .timestamp(LocalDateTime.now())
            .build();
        yield new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
      }
      default -> {
        ErrorResponse errorResponse = ErrorResponse.builder().build();
        yield new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    };
  }

}
