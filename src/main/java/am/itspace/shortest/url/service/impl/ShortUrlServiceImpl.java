package am.itspace.shortest.url.service.impl;

import am.itspace.shortest.url.dto.request.ShortUrlRequest;
import am.itspace.shortest.url.dto.response.ShortUrlResponse;
import am.itspace.shortest.url.dto.response.ShortUrlStatusAndCountResponse;
import am.itspace.shortest.url.exception.UserNotFoundException;
import am.itspace.shortest.url.mapper.ShortUrlMapper;
import am.itspace.shortest.url.model.ShortUrl;
import am.itspace.shortest.url.model.User;
import am.itspace.shortest.url.repository.ShortUrlRepository;
import am.itspace.shortest.url.repository.UserRepository;
import am.itspace.shortest.url.security.CurrentUser;
import am.itspace.shortest.url.service.ShortUrlService;
import am.itspace.shortest.url.util.ShortUrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShortUrlServiceImpl implements ShortUrlService {

  private final ShortUrlRepository shortUrlRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final UserRepository userRepository;

  private static final String KEY_PREFIX = "short_url_";
  private static final String BY_ORIGINAL_PREFIX = KEY_PREFIX + "by_orig:";
  private static final String BY_KEY_PREFIX = KEY_PREFIX + "by_key:";
  private static final Duration CACHE_TTL = Duration.ofHours(24);
  private static final String BY_KEY_CLICK_COUNT_PREFIX = "shortUrl:clicks:";
  private static final String ACTIVE_URLS = "active.urls";


  @Override
  public ShortUrlResponse createShortUrl(ShortUrlRequest request, CurrentUser currentUser) {
    ShortUrl findShortUrlInCache = (ShortUrl) redisTemplate.opsForValue().get(BY_ORIGINAL_PREFIX + request.getOriginalUrl());

    if (findShortUrlInCache != null) {
      return ShortUrlMapper.toShortUrlResponse(findShortUrlInCache);
    }

    User user = userRepository.findById(currentUser.getUser().getId())
        .orElseThrow(() -> new UserNotFoundException("User not found"));

    String originalUrlKey = BY_ORIGINAL_PREFIX + request.getOriginalUrl();
    Optional<ShortUrl> existing = shortUrlRepository.findByOriginalUrl(request.getOriginalUrl());

    if (existing.isPresent()) {
      ShortUrl found = existing.get();
      cacheBoth(found);
      return ShortUrlMapper.toShortUrlResponse(found);
    }

    String shortKey = ShortUrlUtil.generateKey.get();

    ShortUrl shortUrl = ShortUrl.builder()
        .originalUrl(request.getOriginalUrl())
        .shortKey(shortKey)
        .isActive(false)
        .clickCount(0)
        .userId(user.getId())
        .build();

    ShortUrl savedUrl = shortUrlRepository.save(shortUrl);

    ShortUrl cachedShortUrl = (ShortUrl) redisTemplate.opsForValue().get(originalUrlKey);

    if (cachedShortUrl != null) {
      return ShortUrlMapper.toShortUrlResponse(cachedShortUrl);
    }
    cacheBoth(savedUrl);

    redisTemplate.opsForValue().set(KEY_PREFIX + shortKey, savedUrl);
    redisTemplate.opsForValue().set(originalUrlKey, savedUrl);

    return ShortUrlMapper.toShortUrlResponse(savedUrl);
  }

  @Override
  @Cacheable(cacheNames = "shortUrl", key = "#shortKey")
  public String getOriginalUrl(String shortKey) {
    String clickCountKey = BY_KEY_CLICK_COUNT_PREFIX + shortKey;

    ShortUrl cached = (ShortUrl) redisTemplate.opsForValue().get(BY_KEY_PREFIX + shortKey);
    if (cached != null) {
      redisTemplate.opsForValue().increment(clickCountKey);
      return cached.getOriginalUrl();
    }

    return shortUrlRepository.findByShortKey(shortKey)
        .map(ShortUrl::getOriginalUrl)
        .orElse(null);
  }

  @Override
  @Transactional
  public void updateClickCount(String shortKey) {
    redisTemplate.opsForValue().increment(KEY_PREFIX + shortKey);
    redisTemplate.opsForSet().add(ACTIVE_URLS, shortKey);
  }

  @Override
  public Optional<ShortUrlStatusAndCountResponse> getStatusAndClickCount(String shortKey) {
    return shortUrlRepository.findByShortKey(shortKey)
        .map(shortUrl -> ShortUrlMapper.toStatusResponse(shortUrl));
  }

  private void cacheBoth(ShortUrl shortUrl) {
    String byKey = BY_KEY_PREFIX + shortUrl.getShortKey();
    String byOrig = BY_ORIGINAL_PREFIX + shortUrl.getOriginalUrl();

    redisTemplate.opsForValue().set(byKey, shortUrl, CACHE_TTL);
    redisTemplate.opsForValue().set(byOrig, shortUrl, CACHE_TTL);
  }

}
