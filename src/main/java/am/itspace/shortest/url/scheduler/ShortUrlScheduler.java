package am.itspace.shortest.url.scheduler;

import am.itspace.shortest.url.model.ShortUrl;
import am.itspace.shortest.url.repository.ShortUrlRepository;
import am.itspace.shortest.url.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ShortUrlScheduler {

    public static final String SHORTURL_CLICKS = "shorturl:clicks:*";
    public static final String SHORTURL_CLICKS1 = "shorturl:clicks:";
    private final ShortUrlRepository shortUrlRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    private static final String KEY_PREFIX = "shorturl:";
    private static final String ORIGINAL_URL_KEY_PREFIX = "shorturl:original:";

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void clearCache() {
        shortUrlRepository.findByIsActiveFalse()
                .forEach(shortUrl -> {
                    shortUrlRepository.delete(shortUrl);
                    redisTemplate.delete(KEY_PREFIX + shortUrl.getShortKey());
                    redisTemplate.delete(ORIGINAL_URL_KEY_PREFIX + shortUrl.getShortKey());
                });

        shortUrlRepository.findAll()
                .forEach(shortUrl -> {
                    ShortUrl cached = (ShortUrl) redisTemplate.opsForValue().get(ORIGINAL_URL_KEY_PREFIX + shortUrl.getOriginalUrl());
                    if (cached != null) {
                        shortUrl.setShortKey(cached.getShortKey());
                        shortUrlRepository.save(shortUrl);
                    } else {
                        redisTemplate.opsForValue().set(ORIGINAL_URL_KEY_PREFIX + shortUrl.getOriginalUrl(), shortUrl);
                    }
                });
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncClickCount() {
        Set<String> clickKeys = redisTemplate.keys(SHORTURL_CLICKS);

        if (!clickKeys.isEmpty()) {
            for (String key : clickKeys) {
                String shortKey = key.substring(SHORTURL_CLICKS1.length());

                String clickCountStr = redisTemplate.opsForValue().get(key).toString();
                int clickCount = (clickCountStr != null) ? Integer.parseInt(clickCountStr) : 0;

                shortUrlRepository.findByShortKey(shortKey).ifPresent(url -> {

                    url.setClickCount(url.getClickCount() + clickCount);
                    shortUrlRepository.save(url);

                    System.out.println("Updated click count for " + shortKey + " to " + url.getClickCount());

                    redisTemplate.opsForValue().set(key, "0");
                });
            }
        }
    }

}
