package am.itspace.shortest.url.mapper;

import am.itspace.shortest.url.dto.response.ShortUrlResponse;
import am.itspace.shortest.url.dto.response.ShortUrlStatusAndCountResponse;
import am.itspace.shortest.url.model.ShortUrl;

public final class ShortUrlMapper {

    private ShortUrlMapper() {
    }

    public static ShortUrlResponse toShortUrlResponse(ShortUrl shortUrl) {
        return ShortUrlResponse.builder()
                .id(shortUrl.getId())
                .shortKey(shortUrl.getShortKey())
                .originalUrl(shortUrl.getOriginalUrl())
                .clickCount(shortUrl.getClickCount())
                .isActive(shortUrl.getIsActive())
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
