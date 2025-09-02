package am.itspace.shortest.url.scheduler;

import am.itspace.shortest.url.model.ShortUrl;
import am.itspace.shortest.url.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShortUrlScheduler {

  private final ShortUrlRepository shortUrlRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  private static final String KEY_PREFIX = "short_url:";
  private static final String ORIGINAL_URL_KEY_PREFIX = "short_url:original:";
  private static final String SHORT_URL_CLICKS = "short_url:clicks:*";
  private static final String ACTIEVE_URLS = "active.urls";

  @Transactional
  @Scheduled(cron = "${scheduler.cron}")
  public void clearCache() {

    List<ShortUrl> urlsToDelete = shortUrlRepository.findAll();
    List<List<ShortUrl>> partitions = partition(urlsToDelete);

    for (List<ShortUrl> partition : partitions) {
      if (partition.isEmpty()) continue;

      shortUrlRepository.deleteAllInBatch(partition);

      List<String> keysToDelete = new ArrayList<>(partition.size() * 2);
      for (ShortUrl url : partition) {
        keysToDelete.add(KEY_PREFIX + url.getShortKey());
        keysToDelete.add(ORIGINAL_URL_KEY_PREFIX + url.getOriginalUrl());
      }

      if (!keysToDelete.isEmpty()) redisTemplate.delete(keysToDelete);

      List<ShortUrl> urlsToCache = shortUrlRepository.findAll();
      List<List<ShortUrl>> partitionsToCache = partition(urlsToCache);

      for (List<ShortUrl> partitionToCache : partitionsToCache) {

        if (partitionToCache.isEmpty()) continue;

        Map<String, ShortUrl> cacheMap = new HashMap<>();

        for (ShortUrl urlToCache : partitionToCache) {
          redisTemplate.opsForValue().set(KEY_PREFIX + urlToCache.getShortKey(), urlToCache);
          redisTemplate.opsForValue().set(ORIGINAL_URL_KEY_PREFIX + urlToCache.getOriginalUrl(), urlToCache);
        }
        redisTemplate.opsForValue().multiSet(cacheMap);
      }
    }
  }

  @Scheduled(cron = "${scheduler.cron}")
  public void clearInactiveUrls() {
    Set<String> allUrlKeys = redisTemplate.keys(KEY_PREFIX + "*");
    if (allUrlKeys.isEmpty()) {
      return;
    }

    Set<Object> activeUrlKeys = redisTemplate.opsForSet().members("active.urls");
    if (activeUrlKeys == null) {
      activeUrlKeys = Collections.emptySet();
    }

    Set<Object> inactiveUrlKeys = new HashSet<>(allUrlKeys);
    inactiveUrlKeys.removeAll(activeUrlKeys);

    if (!inactiveUrlKeys.isEmpty()) {
      long deletedCount = redisTemplate.delete(Collections.singleton(String.valueOf(inactiveUrlKeys)));
      log.info("Deleted {} inactive URL keys from the cache.", deletedCount);
    }

    redisTemplate.delete(ACTIEVE_URLS);
  }

  @Transactional
  @Scheduled(cron = "${scheduler.cron}")
  public void syncClickCount() {
    Set<String> clickKeys = redisTemplate.keys(SHORT_URL_CLICKS);

    if (clickKeys.isEmpty()) return;

    List<String> shortKeys = new ArrayList<>(clickKeys.size());

    for (String key : clickKeys) {
      shortKeys.add(key.replace(SHORT_URL_CLICKS, ""));
    }

    List<ShortUrl> shortUrlsToUpdate = shortUrlRepository.findAllByShortKeyIn(shortKeys);
    List<Object> value = redisTemplate.opsForValue().multiGet(clickKeys);
    Map<String, Integer> clickCounts = new HashMap<>();

    for (int i = 0; i < shortUrlsToUpdate.size(); i++) {
      clickCounts.put(shortKeys.get(i), Integer.parseInt(Objects.requireNonNull(value).get(i).toString()));
    }

    for (ShortUrl url : shortUrlsToUpdate) {
      Integer count = clickCounts.get(url.getShortKey());
      if (count != null) {
        url.setClickCount(count);
      }
    }

    shortUrlRepository.saveAll(shortUrlsToUpdate);
    redisTemplate.delete(clickKeys);
  }

  private List<List<ShortUrl>> partition(List<ShortUrl> urls) {

    if (urls == null || urls.isEmpty()) return Collections.emptyList();

    List<List<ShortUrl>> partitions = new ArrayList<>();
    int start = 0;
    while (start < urls.size()) {
      int end = Math.min(start + 100, urls.size());
      partitions.add(urls.subList(start, end));
      start = end;
    }
    return partitions;
  }
}
