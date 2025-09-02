package am.itspace.shortest.url.service.impl;

import am.itspace.shortest.url.dto.request.CreateUserRequest;
import am.itspace.shortest.url.dto.request.UserAuthRequest;
import am.itspace.shortest.url.dto.response.UserAuthResponse;
import am.itspace.shortest.url.exception.CredentialException;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.authentication.password.CompromisedPasswordException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ShortUrlRepository shortUrlRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenUtil jwtTokenUtil;

  @Mock
  private TokenRepository tokenRepository;

  @Mock
  private CompromisedPasswordChecker compromisedPasswordChecker;

  private UserServiceImpl userServiceImpl;

  @BeforeEach
  void setUp() {
    userServiceImpl = new UserServiceImpl(
        userRepository,
        jwtTokenUtil,
        passwordEncoder,
        tokenRepository,
        shortUrlRepository,
        compromisedPasswordChecker
    );
  }


  @Test
  void register_whenUserDoesNotExist_createsUserAndReturnsTokens() {
    CreateUserRequest request = CreateUserRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john@example.com")
        .password("password123")
        .originalUrl("https://example.com")
        .build();

    ShortUrl shortUrl = ShortUrl.builder()
        .id(1L)
        .originalUrl(request.getOriginalUrl())
        .build();

    User savedUser = User.builder()
        .id(1L)
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password("encodedPassword")
        .shortUrl(shortUrl)
        .role(Role.USER)
        .build();

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
    when(shortUrlRepository.findByOriginalUrl(request.getOriginalUrl())).thenReturn(Optional.of(shortUrl));
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtTokenUtil.generateToken(savedUser.getEmail())).thenReturn("accessToken123");
    when(jwtTokenUtil.refreshToken("accessToken123")).thenReturn("refreshToken456");

    UserAuthResponse response = userServiceImpl.register(request);

    assertNotNull(response);
    assertEquals("John", response.getFirstName());
    assertEquals("Doe", response.getLastName());
    assertEquals("accessToken123", response.getAccessToken());
    assertEquals("refreshToken456", response.getRefreshToken());

    verify(userRepository).findByEmail(request.getEmail());
    verify(passwordEncoder).encode(request.getPassword());
    verify(shortUrlRepository).findByOriginalUrl(request.getOriginalUrl());
    verify(userRepository).save(any(User.class));
    verify(jwtTokenUtil).generateToken(savedUser.getEmail());
    verify(jwtTokenUtil).refreshToken("accessToken123");
  }

  @Test
  void register_whenUserAlreadyExists_throwsException() {
    CreateUserRequest request = CreateUserRequest.builder()
        .email("john@example.com")
        .build();

    User existingUser = User.builder().email(request.getEmail()).build();
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

    assertThrows(UserAlreadyExistsException.class, () -> userServiceImpl.register(request));

    verify(userRepository).findByEmail(request.getEmail());
    verifyNoMoreInteractions(userRepository, shortUrlRepository, passwordEncoder, jwtTokenUtil);
  }


  @Test
  void login_whenCredentialsAreCorrect_returnsAuthResponse() {
    String email = "test@example.com";
    String rawPassword = "password123";
    String encodedPassword = "encodedPassword";

    User mockUser = User.builder()
        .id(1L)
        .email(email)
        .password(encodedPassword)
        .firstName("Test")
        .lastName("User")
        .role(Role.USER)
        .build();

    UserAuthRequest request = new UserAuthRequest(email, rawPassword);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
    when(compromisedPasswordChecker.check(rawPassword))
        .thenReturn(new CompromisedPasswordDecision(false));
    when(jwtTokenUtil.generateToken(email)).thenReturn("access-token");
    when(jwtTokenUtil.refreshToken("access-token")).thenReturn("refresh-token");

    UserAuthResponse response = userServiceImpl.login(request);

    assertNotNull(response);
    assertEquals("access-token", response.getAccessToken());
    assertEquals("refresh-token", response.getRefreshToken());
    assertEquals(mockUser.getFirstName(), response.getFirstName());
    assertEquals(mockUser.getLastName(), response.getLastName());

    verify(userRepository).findByEmail(email);
    verify(passwordEncoder).matches(rawPassword, encodedPassword);
    verify(compromisedPasswordChecker).check(rawPassword);
    verify(jwtTokenUtil).generateToken(email);
    verify(jwtTokenUtil).refreshToken("access-token");
    verify(tokenRepository).save(any());
  }

  @Test
  void login_whenUserNotFound_throwsCredentialException() {
    UserAuthRequest request = new UserAuthRequest("unknown@example.com", "password");

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThrows(CredentialException.class, () -> userServiceImpl.login(request));

    verify(userRepository).findByEmail("unknown@example.com");
    verifyNoInteractions(passwordEncoder, jwtTokenUtil, compromisedPasswordChecker, tokenRepository);
  }

  @Test
  void login_whenPasswordIsWrong_throwsCredentialException() {
    String email = "test@example.com";
    String rawPassword = "wrongPassword";
    String encodedPassword = "encodedPassword";

    User mockUser = User.builder()
        .id(1L)
        .email(email)
        .password(encodedPassword)
        .build();

    UserAuthRequest request = new UserAuthRequest(email, rawPassword);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

    assertThrows(CredentialException.class, () -> userServiceImpl.login(request));

    verify(userRepository).findByEmail(email);
    verify(passwordEncoder).matches(rawPassword, encodedPassword);
    verifyNoInteractions(jwtTokenUtil, compromisedPasswordChecker, tokenRepository);
  }

  @Test
  void login_whenPasswordIsCompromised_throwsCompromisedPasswordException() {
    String email = "test@example.com";
    String rawPassword = "password123";
    String encodedPassword = "encodedPassword";

    User mockUser = User.builder()
        .id(1L)
        .email(email)
        .password(encodedPassword)
        .build();

    UserAuthRequest request = new UserAuthRequest(email, rawPassword);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
    when(compromisedPasswordChecker.check(rawPassword))
        .thenReturn(new CompromisedPasswordDecision(true));

    assertThrows(CompromisedPasswordException.class, () -> userServiceImpl.login(request));

    verify(userRepository).findByEmail(email);
    verify(passwordEncoder).matches(rawPassword, encodedPassword);
    verify(compromisedPasswordChecker).check(rawPassword);
    verifyNoInteractions(jwtTokenUtil, tokenRepository);
  }

}