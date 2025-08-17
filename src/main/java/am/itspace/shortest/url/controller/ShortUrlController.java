package am.itspace.shortest.url.controller;

import am.itspace.shortest.url.dto.ShortUrlRequest;
import am.itspace.shortest.url.dto.ShortUrlResponse;
import am.itspace.shortest.url.dto.ShortUrlStatusAndCountResponse;
import am.itspace.shortest.url.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ShortUrlController {

  private final ShortUrlService shortUrlService;

  @PostMapping("/shorten")
  public ResponseEntity<ShortUrlResponse> createShortUrl(@RequestBody ShortUrlRequest originalUrl) {
    ShortUrlResponse response = shortUrlService.createShortUrl(originalUrl);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{shortKey}")
  public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
    Optional<String> originalUrlOptional = shortUrlService.getOriginalUrl(shortKey);

    if (originalUrlOptional.isPresent()) {
      String originalUrl = originalUrlOptional.get();
      shortUrlService.updateClickCount(shortKey);
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(URI.create(originalUrl));
      return new ResponseEntity<>(headers, HttpStatus.FOUND);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/status/{shortKey}")
  public ResponseEntity<ShortUrlStatusAndCountResponse> getStatusAndClickCount(@PathVariable String shortKey) {
    Optional<ShortUrlStatusAndCountResponse> statusAndCountResponse = shortUrlService.getStatusAndClickCount(shortKey);
    return statusAndCountResponse.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

}
