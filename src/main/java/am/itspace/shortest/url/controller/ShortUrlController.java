package am.itspace.shortest.url.controller;

import am.itspace.shortest.url.dto.request.ShortUrlRequest;
import am.itspace.shortest.url.dto.response.ShortUrlResponse;
import am.itspace.shortest.url.dto.response.ShortUrlStatusAndCountResponse;
import am.itspace.shortest.url.security.CurrentUser;
import am.itspace.shortest.url.service.ShortUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ShortUrlController {

  private static final Logger log = LoggerFactory.getLogger(ShortUrlController.class);
  private final ShortUrlService shortUrlService;

  @PostMapping("/shorten")
  public ResponseEntity<ShortUrlResponse> createShortUrl(@RequestBody @Valid ShortUrlRequest originalUrl, @AuthenticationPrincipal CurrentUser currentUser) {
    ShortUrlResponse response = shortUrlService.createShortUrl(originalUrl, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{shortKey}")
  public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
    String originalUrl = shortUrlService.getOriginalUrl(shortKey);

    if (originalUrl != null) {
      log.info("Redirecting to {}", originalUrl);

      shortUrlService.updateClickCount(shortKey);
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(URI.create(originalUrl));
      return new ResponseEntity<>(headers, HttpStatus.FOUND);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/status/{shortKey}")
  public ResponseEntity<ShortUrlStatusAndCountResponse> getStatusAndClickCount(@PathVariable String shortKey, @AuthenticationPrincipal CurrentUser currentUser) {
    Optional<ShortUrlStatusAndCountResponse> statusAndCountResponse = shortUrlService.getStatusAndClickCount(shortKey);
    return statusAndCountResponse.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

}
