package am.itspace.shortest.url.service.impl;

import am.itspace.shortest.url.dto.request.ShortUrlRequest;
import am.itspace.shortest.url.dto.response.ShortUrlResponse;
import am.itspace.shortest.url.exception.UserNotFoundException;
import am.itspace.shortest.url.mapper.ShortUrlMapper;
import am.itspace.shortest.url.model.ShortUrl;
import am.itspace.shortest.url.model.User;
import am.itspace.shortest.url.model.enums.Role;
import am.itspace.shortest.url.repository.ShortUrlRepository;
import am.itspace.shortest.url.repository.UserRepository;
import am.itspace.shortest.url.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceImplTest {

  private static final String KEY_PREFIX = "shorturl:";
  private static final String BY_ORIGINAL_PREFIX = "shorturl:original:";

  @Mock
  private UserRepository userRepository;
  @Mock
  private ShortUrlRepository shortUrlRepository;

  @Mock
  private RedisTemplate<String, Object> redisTemplate;
  @Mock
  private ValueOperations<String, Object> valueOperations;

  @InjectMocks
  private UserServiceImpl userService;

  @InjectMocks
  private ShortUrlServiceImpl shortUrlService;

  private void mockRedisValueOperations() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  void createShortUrl_whenUserNotFound_throwsUserNotFoundException() {
    ShortUrlRequest request = ShortUrlRequest.builder()
        .originalUrl("https://example.com")
        .build();

    User mockUser = User.builder()
        .id(99L)
        .firstName("Test")
        .lastName("User")
        .email("test@example.com")
        .password("testpassword")
        .role(Role.USER)
        .build();
    CurrentUser currentUser = new CurrentUser(mockUser);

    // FIXED: return Optional.empty() instead of null
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> shortUrlService.createShortUrl(request, currentUser)
    );

    verify(userRepository).findById(currentUser.getUser().getId());
    verifyNoInteractions(shortUrlRepository, redisTemplate);
  }

  @Test
  void createShortUrl_whenShortUrlAlreadyExists_returnsExistingShortUrlAndCaches() {
    String originalUrl = "https://existing.com";
    String shortKey = "xyz123";
    ShortUrlRequest request = ShortUrlRequest.builder().originalUrl(originalUrl).build();
    User mockUser = User.builder()
        .id(99L)
        .firstName("Test")
        .lastName("User")
        .email("test@example.com")
        .password("testpassword")
        .role(Role.USER)
        .build();
    CurrentUser currentUser = new CurrentUser(mockUser);

    ShortUrl existingShortUrl = ShortUrl.builder()
        .id(100L)
        .originalUrl(originalUrl)
        .shortKey(shortKey)
        .isActive(true)
        .clickCount(5)
        .userId(mockUser.getId())
        .build();
    ShortUrlResponse expectedResponse = ShortUrlResponse.builder()
        .originalUrl(originalUrl)
        .shortKey(shortKey)
        .build();

    mockRedisValueOperations();
    when(userRepository.findById(currentUser.getUser().getId())).thenReturn(Optional.of(mockUser));
    when(shortUrlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existingShortUrl));

    doNothing().when(valueOperations).set(
        eq(KEY_PREFIX + shortKey),
        eq(existingShortUrl),
        any(Duration.class)
    );
    doNothing().when(valueOperations).set(
        eq(BY_ORIGINAL_PREFIX + originalUrl),
        eq(existingShortUrl),
        any(Duration.class)
    );

    try (MockedStatic<ShortUrlMapper> mockedShortUrlMapper = mockStatic(ShortUrlMapper.class)) {
      mockedShortUrlMapper.when(() -> ShortUrlMapper.toShortUrlResponse(existingShortUrl)).thenReturn(expectedResponse);

      ShortUrlResponse response = shortUrlService.createShortUrl(request, currentUser);

      assertNotNull(response);
      assertEquals(expectedResponse.getShortKey(), response.getShortKey());
      assertEquals(expectedResponse.getOriginalUrl(), response.getOriginalUrl());

      verify(userRepository).findById(currentUser.getUser().getId());
      verify(shortUrlRepository).findByOriginalUrl(originalUrl);
      verify(valueOperations).set(eq(KEY_PREFIX + shortKey), eq(existingShortUrl), any(Duration.class));
      verify(valueOperations).set(eq(BY_ORIGINAL_PREFIX + originalUrl), eq(existingShortUrl), any(Duration.class));
    }
  }
}