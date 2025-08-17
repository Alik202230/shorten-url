package am.itspace.shortest.url.service;

import am.itspace.shortest.url.dto.RefreshTokenResponse;
import am.itspace.shortest.url.dto.SaveUserRequest;
import am.itspace.shortest.url.dto.UserAuthRequest;
import am.itspace.shortest.url.dto.UserAuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public interface UserService {

  void register(SaveUserRequest request);

  Optional<UserAuthResponse> login(UserAuthRequest request);

  RefreshTokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

}
