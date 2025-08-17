package am.itspace.shortest.url.service.impl;

import am.itspace.shortest.url.dto.ShortUrlRequest;
import am.itspace.shortest.url.dto.ShortUrlResponse;
import am.itspace.shortest.url.dto.ShortUrlStatusAndCountResponse;
import am.itspace.shortest.url.mapper.ShortUrlMapper;
import am.itspace.shortest.url.model.ShortUrl;
import am.itspace.shortest.url.repository.ShortUrlRepository;
import am.itspace.shortest.url.service.ShortUrlService;
import am.itspace.shortest.url.util.ShortUrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShortUrlServiceImpl implements ShortUrlService {

  private final ShortUrlRepository shortUrlRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private static final String KEY_PREFIX = "short_url_";
  private static final String BY_ORIGINAL_PREFIX = KEY_PREFIX + "by_orig:";
  private static final String BY_KEY_PREFIX = KEY_PREFIX + "by_key:";
  private static final Duration CACHE_TTL = Duration.ofHours(24); // optional


  @Override
  public ShortUrlResponse createShortUrl(ShortUrlRequest request) {

    String originalUrlKey = BY_ORIGINAL_PREFIX + request.getOriginalUrl();
    ShortUrl cachedShortUrl = (ShortUrl) redisTemplate.opsForValue().get(originalUrlKey);

    if (cachedShortUrl != null) {
      return ShortUrlMapper.toShortUrlResponse(cachedShortUrl);
    }

    Optional<ShortUrl> existing = shortUrlRepository.findByOriginalUrl(request.getOriginalUrl());

    if (existing.isPresent()) {
      ShortUrl found = existing.get();
      cacheBoth(found);
      return ShortUrlMapper.toShortUrlResponse(found);

    }

    String shortKey;

    do {
      shortKey = ShortUrlUtil.generateKey.get();
    } while (shortUrlRepository.existsByShortKey(shortKey));

    ShortUrl shortUrl = ShortUrl.builder()
        .originalUrl(request.getOriginalUrl())
        .shortKey(shortKey)
        .isActive(false)
        .clickCount(0)
        .build();

    ShortUrl savedUrl = shortUrlRepository.save(shortUrl);

    cacheBoth(savedUrl);

    redisTemplate.opsForValue().set(KEY_PREFIX + shortKey, savedUrl);
    redisTemplate.opsForValue().set(originalUrlKey, savedUrl);

    return ShortUrlMapper.toShortUrlResponse(savedUrl);
  }

  @Override
  @Cacheable(cacheNames = "shortUrl", key = "#shortKey")
  public Optional<String> getOriginalUrl(String shortKey) {
    ShortUrl cached = (ShortUrl) redisTemplate.opsForValue().get(BY_KEY_PREFIX + shortKey);
    if (cached != null) {
      redisTemplate.opsForValue().increment(BY_KEY_PREFIX + shortKey);
      return Optional.of(cached.getOriginalUrl());
    }
    return shortUrlRepository.findByShortKey(shortKey)
        .map(ShortUrl::getOriginalUrl);
  }

  @Override
  public void updateClickCount(String shortKey) {
    shortUrlRepository.findByShortKey(shortKey)
        .ifPresent(shortUrl -> {
          shortUrl.setClickCount(shortUrl.getClickCount() + 1);
          shortUrl.setIsActive(true);
          shortUrlRepository.save(shortUrl);
        });
  }

  @Override
  public Optional<ShortUrlStatusAndCountResponse> getStatusAndClickCount(String shortKey) {
    return shortUrlRepository.findByShortKey(shortKey)
        .map(shortUrl -> ShortUrlMapper.toStatusResponse(shortUrl));
  }

  private void cacheBoth(ShortUrl shortUrl) {
    String byKey = BY_KEY_PREFIX + shortUrl.getShortKey();
    String byOrig = BY_ORIGINAL_PREFIX + shortUrl.getOriginalUrl();
    if (CACHE_TTL != null) {
      redisTemplate.opsForValue().set(byKey, shortUrl, CACHE_TTL);
      redisTemplate.opsForValue().set(byOrig, shortUrl, CACHE_TTL);
    } else {
      redisTemplate.opsForValue().set(byKey, shortUrl);
      redisTemplate.opsForValue().set(byOrig, shortUrl);
    }

  }

}
