package am.itspace.shortest.url.mapper;

import am.itspace.shortest.url.dto.ShortUrlResponse;
import am.itspace.shortest.url.dto.ShortUrlStatusAndCountResponse;
import am.itspace.shortest.url.model.ShortUrl;

public final class ShortUrlMapper {

  private ShortUrlMapper() {}

  public static ShortUrlResponse toShortUrlResponse(ShortUrl shortUrl) {
    return ShortUrlResponse.builder()
        .id(shortUrl.getId())
        .shortKey(shortUrl.getShortKey())
        .originalUrl(shortUrl.getOriginalUrl())
        .clickCount(shortUrl.getClickCount())
        .isActive(shortUrl.getIsActive())
        .user(shortUrl.getUser())
        .build();
  }

  public static ShortUrlStatusAndCountResponse toStatusResponse(ShortUrl shortUrl) {
    return ShortUrlStatusAndCountResponse.builder()
        .shortKey(shortUrl.getShortKey())
        .isActive(shortUrl.getIsActive())
        .clickCount(shortUrl.getClickCount())
        .build();
  }

}
