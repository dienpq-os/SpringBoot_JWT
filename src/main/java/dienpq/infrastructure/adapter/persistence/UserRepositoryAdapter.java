package dienpq.infrastructure.adapter.persistence;

import dienpq.domain.model.User;
import dienpq.domain.port.repository.UserRepositoryPort;
import dienpq.infrastructure.adapter.persistence.entity.UserEntity;
import dienpq.infrastructure.adapter.persistence.mapper.UserMapper;
import dienpq.infrastructure.adapter.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackOn = Exception.class) // Bảo vệ tiến trình đăng ký tài khoản
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jpaRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailOrUsername(String email, String username) {
        return jpaRepository.findByEmailOrUsername(email, username).map(userMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}