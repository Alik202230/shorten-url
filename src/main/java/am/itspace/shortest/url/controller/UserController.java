package am.itspace.shortest.url.controller;


import am.itspace.shortest.url.dto.request.SaveUserRequest;
import am.itspace.shortest.url.dto.request.UserAuthRequest;
import am.itspace.shortest.url.dto.response.RefreshTokenResponse;
import am.itspace.shortest.url.dto.response.UserAuthResponse;
import am.itspace.shortest.url.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<Void> register(@RequestBody @Valid SaveUserRequest saveUserRequest) {
    userService.register(saveUserRequest);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<UserAuthResponse> login(@RequestBody @Valid UserAuthRequest userAuthRequest) {
    UserAuthResponse response = userService.login(userAuthRequest);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/refresh-token")
  public RefreshTokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
    return userService.refreshToken(request, response);
  }

}
