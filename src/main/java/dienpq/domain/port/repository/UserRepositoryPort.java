package dienpq.domain.port.repository;

import dienpq.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Integer id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    List<User> findAll();

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void deleteById(Integer id);
}