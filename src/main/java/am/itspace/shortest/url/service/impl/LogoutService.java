package am.itspace.shortest.url.service.impl;

import am.itspace.shortest.url.model.Token;
import am.itspace.shortest.url.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

  private final TokenRepository tokenRepository;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    final String requestHeader = request.getHeader("Authorization");
    final String token;

    if (requestHeader == null || !requestHeader.startsWith("Bearer ")) {
      return;
    }
    token = requestHeader.substring(7);

    Token optionalToken = tokenRepository.findByAccessToken(token)
        .orElse(null);

    if (optionalToken != null) {
      optionalToken.setRevoked(true);
      optionalToken.setExpired(true);
      tokenRepository.save(optionalToken);
    }

  }

}
