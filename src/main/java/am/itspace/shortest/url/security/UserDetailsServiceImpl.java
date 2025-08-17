package am.itspace.shortest.url.security;

import am.itspace.shortest.url.model.User;
import am.itspace.shortest.url.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<User> optionalUser = userRepository.findByEmail(username);
    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      return new CurrentUser(user);
    }
    throw new UsernameNotFoundException(username);
  }

}
