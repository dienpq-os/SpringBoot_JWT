package dienpq.infrastructure.adapter.external;

import dienpq.domain.model.User;
import dienpq.domain.port.repository.UserRepositoryPort;
import dienpq.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpringUserAdapter implements UserDetailsService {
        private final UserRepositoryPort userRepository;

        @Override
        public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
                // Tra cứu linh hoạt bằng cả 2 trường dữ liệu
                User user = userRepository.findByEmailOrUsername(input, input)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + input));

                // Trả về thực thể lưu trữ đồng thời cả Username lẫn Email
                return new CustomUserDetails(user);
        }
}