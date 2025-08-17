package am.itspace.shortest.url.service;

import am.itspace.shortest.url.dto.ShortUrlRequest;
import am.itspace.shortest.url.dto.ShortUrlResponse;
import am.itspace.shortest.url.dto.ShortUrlStatusAndCountResponse;

import java.util.Optional;

public interface ShortUrlService {

  ShortUrlResponse createShortUrl(ShortUrlRequest request);
  Optional<String> getOriginalUrl(String shortKey);
  void updateClickCount(String shortKey);
  Optional<ShortUrlStatusAndCountResponse> getStatusAndClickCount(String shortKey);

}
