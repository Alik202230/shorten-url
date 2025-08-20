package am.itspace.shortest.url.service.impl;

import am.itspace.shortest.url.dto.request.SaveUserRequest;
import am.itspace.shortest.url.dto.request.UserAuthRequest;
import am.itspace.shortest.url.dto.response.UserAuthResponse;
import am.itspace.shortest.url.exception.EmailOrPasswordException;
import am.itspace.shortest.url.exception.UserAlreadyExistsException;
import am.itspace.shortest.url.exception.UserNotFoundException;
import am.itspace.shortest.url.model.ShortUrl;
import am.itspace.shortest.url.model.Token;
import am.itspace.shortest.url.model.User;
import am.itspace.shortest.url.model.enums.Role;
import am.itspace.shortest.url.model.enums.TokenType;
import am.itspace.shortest.url.repository.ShortUrlRepository;
import am.itspace.shortest.url.repository.TokenRepository;
import am.itspace.shortest.url.repository.UserRepository;
import am.itspace.shortest.url.util.jwt.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private JwtTokenUtil jwtTokenUtil;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private TokenRepository tokenRepository;
  @Mock
  private ShortUrlRepository shortUrlRepository;

  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(userRepository, jwtTokenUtil, passwordEncoder, tokenRepository, shortUrlRepository);
  }

  @Test
  void register_whenEmailExists_throwsUserAlreadyExistsException() {
    SaveUserRequest req = SaveUserRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@example.com")
        .password("StrongP@ssw0rd")
        .originalUrl("https://example.com/page")
        .build();

    when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(User.builder().build()));

    assertThrows(UserAlreadyExistsException.class, () -> userService.register(req));

    verify(userRepository).findByEmail(req.getEmail());
    verifyNoMoreInteractions(userRepository, passwordEncoder, jwtTokenUtil, tokenRepository, shortUrlRepository);
  }

  @Test
  void register_whenShortUrlExists_reusesShortUrl_andSavesUserAndToken() {
    SaveUserRequest req = SaveUserRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@example.com")
        .password("StrongP@ssw0rd")
        .originalUrl("https://example.com/page")
        .build();

    when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(req.getPassword())).thenReturn("encodedPassword");

    ShortUrl shortUrl = ShortUrl.builder()
        .id(1L)
        .shortKey("shortKey")
        .originalUrl(req.getOriginalUrl())
        .build();
    when(shortUrlRepository.findByOriginalUrl(req.getOriginalUrl())).thenReturn(Optional.of(shortUrl));

    // Define variables for the mocked tokens
    String accessToken = "mocked-access-token";
    String refreshToken = "mocked-refresh-token";

    // Mock JWT token generation to return the defined tokens
    when(jwtTokenUtil.generateToken(req.getEmail())).thenReturn(accessToken);
    when(jwtTokenUtil.refreshToken(accessToken)).thenReturn(refreshToken);

    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

    userService.register(req);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertEquals("John", savedUser.getFirstName());
    assertEquals("Doe", savedUser.getLastName());
    assertEquals("john.doe@example.com", savedUser.getEmail());
    assertEquals("encodedPassword", savedUser.getPassword());
    assertEquals(Role.USER, savedUser.getRole());
    assertEquals(shortUrl.getId(), savedUser.getShortUrl().getId());

    verify(jwtTokenUtil).generateToken(req.getEmail());

    verify(jwtTokenUtil).refreshToken(accessToken);

    verify(tokenRepository).save(argThat(t ->
        accessToken.equals(t.getAccessToken()) &&
            refreshToken.equals(t.getRefreshToken()) &&
            TokenType.BEARER == t.getType() &&
            !t.isExpired() &&
            !t.isRevoked() &&
            t.getUser() != null
    ));

    verify(shortUrlRepository, never()).save(any(ShortUrl.class));
  }

  @Test
  void register_whenShortUrlNotExists_createsShortUrl_andSavesUserAndTokens() {
    SaveUserRequest req = SaveUserRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@example.com")
        .password("StrongP@ssw0rd")
        .originalUrl("https://example.com/page")
        .build();

    when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(req.getPassword())).thenReturn("ENCODED");
    when(shortUrlRepository.findByOriginalUrl(req.getOriginalUrl())).thenReturn(Optional.empty());

    ShortUrl savedShortUrl = ShortUrl.builder()
        .id(10L)
        .originalUrl(req.getOriginalUrl())
        .shortKey("xyz789")
        .build();
    when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(savedShortUrl);

    String accessToken = "mocked-access-token";
    String refreshToken = "mocked-refresh-token";
    when(jwtTokenUtil.generateToken(req.getEmail())).thenReturn(accessToken);
    when(jwtTokenUtil.refreshToken(accessToken)).thenReturn(refreshToken);

    when(userRepository.save(any(User.class))).thenAnswer(inv -> {
      User u = inv.getArgument(0);
      u.setId(100L);
      return u;
    });
    when(tokenRepository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));

    userService.register(req);

    verify(shortUrlRepository).findByOriginalUrl(req.getOriginalUrl());

    ArgumentCaptor<ShortUrl> shortUrlCaptor = ArgumentCaptor.forClass(ShortUrl.class);
    verify(shortUrlRepository).save(shortUrlCaptor.capture());
    ShortUrl capturedShortUrl = shortUrlCaptor.getValue();
    assertNull(capturedShortUrl.getId(), "ID should be null before saving");
    assertEquals(req.getOriginalUrl(), capturedShortUrl.getOriginalUrl());

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertEquals("John", savedUser.getFirstName());
    assertEquals("Doe", savedUser.getLastName());
    assertEquals("john.doe@example.com", savedUser.getEmail());
    assertEquals("ENCODED", savedUser.getPassword());
    assertEquals(Role.USER, savedUser.getRole());
    assertNotNull(savedUser.getShortUrl());
    assertEquals(savedShortUrl.getId(), savedUser.getShortUrl().getId());

    verify(jwtTokenUtil).generateToken(req.getEmail());
    verify(jwtTokenUtil).refreshToken(accessToken);

    ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
    verify(tokenRepository).save(tokenCaptor.capture());
    Token savedToken = tokenCaptor.getValue();

    assertEquals(accessToken, savedToken.getAccessToken());
    assertEquals(refreshToken, savedToken.getRefreshToken());
    assertEquals(TokenType.BEARER, savedToken.getType());
    assertFalse(savedToken.isExpired());
    assertFalse(savedToken.isRevoked());
    assertNotNull(savedToken.getUser());
    assertEquals(savedUser.getId(), savedToken.getUser().getId());
  }

  @Test
  void login_whenUserExistsAndCredentialsAreCorrect_returnsUserAuthResponse() {
    UserAuthRequest authRequest = UserAuthRequest.builder()
        .email("test@example.com")
        .password("password123")
        .build();

    User user = User.builder()
        .id(1L)
        .email("test@example.com")
        .password("encodedPassword")
        .firstName("Test")
        .build();

    Token oldToken = Token.builder()
        .id(100L)
        .user(user)
        .accessToken("oldAccessToken")
        .type(TokenType.BEARER)
        .revoked(false)
        .isExpired(false)
        .build();


    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);
    when(jwtTokenUtil.generateToken(user.getEmail())).thenReturn("mockedAccessToken");
    when(jwtTokenUtil.refreshToken("mockedAccessToken")).thenReturn("mockedRefreshToken");
    when(tokenRepository.findAllValidTokensByUserId(user.getId())).thenReturn(List.of(oldToken));
    when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Optional<UserAuthResponse> response = userService.login(authRequest);

    assertTrue(response.isPresent(), "Response should be present for successful login");
    UserAuthResponse authResponse = response.get();
    assertEquals("mockedAccessToken", authResponse.getAccessToken());
    assertEquals("mockedRefreshToken", authResponse.getRefreshToken());
    assertEquals("Test", authResponse.getFirstName());

    verify(userRepository).findByEmail(authRequest.getEmail());
    verify(passwordEncoder).matches(authRequest.getPassword(), user.getPassword());
    verify(jwtTokenUtil).generateToken(user.getEmail());
    verify(jwtTokenUtil).refreshToken("mockedAccessToken");

    verify(tokenRepository).findAllValidTokensByUserId(user.getId());

    verify(tokenRepository).saveAll(argThat(tokens -> {
      List<Token> tokenList = StreamSupport.stream(tokens.spliterator(), false).toList();
      return tokenList.size() == 1 &&
          tokenList.get(0).isRevoked() &&
          tokenList.get(0).isExpired() &&
          "oldAccessToken".equals(tokenList.get(0).getAccessToken());
    }));

    verify(tokenRepository).save(argThat(token ->
        "mockedAccessToken".equals(token.getAccessToken()) &&
            !token.isRevoked() &&
            !token.isExpired()
    ));
  }

  @Test
  void login_whenUserDoesNotExist_throwsUserNotFoundException() {
    // Given
    UserAuthRequest authRequest = UserAuthRequest.builder()
        .email("nonexistent@example.com")
        .password("password123")
        .build();

    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

    UserNotFoundException thrown = assertThrows(
        UserNotFoundException.class,
        () -> userService.login(authRequest),
        "Expected UserNotFoundException to be thrown for non-existent user"
    );

    assertEquals("User with email " + authRequest.getEmail() + " not found", thrown.getMessage());

    verify(userRepository).findByEmail(authRequest.getEmail());
    verifyNoMoreInteractions(passwordEncoder, jwtTokenUtil, tokenRepository);
  }

  @Test
  void login_whenPasswordIsIncorrect_throwsEmailOrPasswordException() {
    UserAuthRequest authRequest = UserAuthRequest.builder()
        .email("test@example.com")
        .password("wrongpassword")
        .build();

    User user = User.builder()
        .id(1L)
        .email("test@example.com")
        .password("encodedPassword")
        .firstName("Test")
        .build();

    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(false);

    EmailOrPasswordException thrown = assertThrows(
        EmailOrPasswordException.class,
        () -> userService.login(authRequest),
        "Expected EmailOrPasswordException for incorrect password"
    );

    assertEquals("Wrong password or email", thrown.getMessage());

    verify(userRepository).findByEmail(authRequest.getEmail());
    verify(passwordEncoder).matches(authRequest.getPassword(), user.getPassword());
    verifyNoMoreInteractions(jwtTokenUtil, tokenRepository);
  }

}