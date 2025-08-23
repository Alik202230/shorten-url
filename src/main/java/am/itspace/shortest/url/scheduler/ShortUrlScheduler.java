package am.itspace.shortest.url.scheduler;

import am.itspace.shortest.url.model.ShortUrl;
import am.itspace.shortest.url.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ShortUrlScheduler {

  public static final String SHORTURL_CLICKS = "shorturl:clicks:*";
  public static final String SHORTURL_CLICKS1 = "shorturl:clicks:";
  private final ShortUrlRepository shortUrlRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  private static final String KEY_PREFIX = "shorturl:";
  private static final String ORIGINAL_URL_KEY_PREFIX = "shorturl:original:";

  @Transactional
  @Scheduled(fixedRate = 60000)
  public void clearCache() {

    List<ShortUrl> urlsToDelete = shortUrlRepository.findAll();
    List<List<ShortUrl>> partitions = partition(urlsToDelete, 100);

    for (List<ShortUrl> partition : partitions) {
      if (partition.isEmpty()) continue;

      shortUrlRepository.deleteAll(partition);

      List<String> keysToDelete = new ArrayList<>(partition.size() * 2);
      for (ShortUrl url : partition) {
        keysToDelete.add(KEY_PREFIX + url.getShortKey());
        keysToDelete.add(ORIGINAL_URL_KEY_PREFIX + url.getOriginalUrl());
      }

      if (!keysToDelete.isEmpty()) redisTemplate.delete(keysToDelete);

      List<ShortUrl> urlsToCache = shortUrlRepository.findAll();
      List<List<ShortUrl>> partitionsToCache = partition(urlsToCache, 100);

      for (List<ShortUrl> partitionToCache : partitionsToCache) {
        for (ShortUrl urlToCache : partitionToCache) {
          redisTemplate.opsForValue().set(KEY_PREFIX + urlToCache.getShortKey(), urlToCache);
          redisTemplate.opsForValue().set(ORIGINAL_URL_KEY_PREFIX + urlToCache.getOriginalUrl(), urlToCache);
        }
      }

    }
  }

  @Scheduled(fixedRate = 60000)
  @Transactional
  public void syncClickCount() {
    Set<String> clickKeys = redisTemplate.keys(SHORTURL_CLICKS);

    for(String key : clickKeys){

      String shortKey = key.substring(SHORTURL_CLICKS.length());
      String count = Objects.requireNonNull(redisTemplate.opsForValue().get(clickKeys)).toString();
      Integer clickCount = Integer.valueOf(count);

      shortUrlRepository.findByShortKey(shortKey)
          .ifPresent(shortUrl -> {
            shortUrl.setClickCount(shortUrl.getClickCount() + clickCount);
          });
    }
  }


  private List<List<ShortUrl>> partition(List<ShortUrl> urls, int size) {

    if (urls == null || urls.isEmpty() || size <= 0) return Collections.emptyList();

    List<List<ShortUrl>> partitions = new ArrayList<>();
    int start = 0;
    while (start < urls.size()) {
      int end = Math.min(start + size, urls.size());
      partitions.add(urls.subList(start, end));
      start = end;
    }
    return partitions;
  }

}
