package am.itspace.shortest.url.service;

import am.itspace.shortest.url.dto.request.SaveUserRequest;
import am.itspace.shortest.url.dto.request.UserAuthRequest;
import am.itspace.shortest.url.dto.response.RefreshTokenResponse;
import am.itspace.shortest.url.dto.response.UserAuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface UserService {

  void register(SaveUserRequest request);

  UserAuthResponse login(UserAuthRequest request);

  RefreshTokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

}
