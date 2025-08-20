package am.itspace.shortest.url.config;

import am.itspace.shortest.url.filter.JwtAuthenticationTokenFilter;
import am.itspace.shortest.url.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;
  private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
  private final LogoutHandler logoutHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint)
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/shorten").permitAll()
            .requestMatchers("/{shortKey}").permitAll()
            .requestMatchers("/status/{shortKey}").permitAll()
            .requestMatchers("/user/register").permitAll()
            .requestMatchers("/user/login").permitAll()
            .requestMatchers("/user/refresh-token").permitAll()
        )
        .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .rememberMe(remember -> remember.key("nfownfownf")
            .tokenValiditySeconds(233344)
            .rememberMeParameter("remember-me")
            .rememberMeCookieName("weomoefmeof"))
        .logout()
        .logoutUrl("/user/logout")
        .addLogoutHandler(logoutHandler)
        .logoutSuccessHandler((request, response, authentication) ->
            SecurityContextHolder.clearContext());
    return httpSecurity.build();
  }


  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
