package dienpq.infrastructure.adapter.external;

import dienpq.domain.model.User;
import dienpq.domain.port.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpringUserAdapter implements UserDetailsService {
        private final UserRepositoryPort userRepository;

        @Override
        public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
                User user = userRepository.findByEmailOrUsername(input, input)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + input));

                return new org.springframework.security.core.userdetails.User(
                                user.getUsername(),
                                user.getPassword(),
                                java.util.Collections.singletonList(
                                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                                "ROLE_" + user.getRole())));
        }
}