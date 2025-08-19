package am.itspace.shortest.url.filter;

import am.itspace.shortest.url.repository.TokenRepository;
import am.itspace.shortest.url.util.jwt.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

  private final JwtTokenUtil jwtTokenUtil;
  private final UserDetailsService userDetailsService;
  private final TokenRepository tokenRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, IOException {
    String requestHeader = request.getHeader("Authorization");

    String username = null;
    String authToken = null;
    if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
      authToken = requestHeader.substring(7);
      try {
        username = jwtTokenUtil.getUsernameFromToken(authToken);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

      boolean isTokenValid = this.tokenRepository.findByAccessToken(authToken)
          .map(token -> !token.isExpired() && !token.isRevoked())
          .orElse(false);

      if (jwtTokenUtil.validateToken(authToken, userDetails.getUsername()) && isTokenValid) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

}
