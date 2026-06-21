package dienpq.infrastructure.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dienpq.domain.model.User;
import dienpq.domain.port.repository.UserRepositoryPort;
import dienpq.domain.port.external.PasswordServicePort;

@Configuration
public class UsersInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepositoryPort userRepository, PasswordServicePort passwordEncoder) {
        return args -> {
            String Email = "dienpq@gmail.com";
            String Username = "admin";
            String role = "ROLE_ADMIN";
            String pas = passwordEncoder.encode("admin123");
            if (userRepository.findByEmailOrUsername(Email, Username).isEmpty()) {
                User admin = new User();
                admin.initializeNewUser(Username, Email, role, pas, null);
                userRepository.save(admin);
            }
        };
    }
}