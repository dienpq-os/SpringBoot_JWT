package dienpq.infrastructure.adapter.persistence.mapper;

import dienpq.application.dto.UserDTO;
import dienpq.domain.model.User;
import dienpq.infrastructure.adapter.persistence.entity.UserEntity;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // 1. Chuẩn hóa tham số đầu vào rõ ràng, an toàn về kiểu dữ liệu (Type-safe)
    public User toDomain(UserEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        // Sử dụng Builder để nạp dữ liệu vào Rich Domain không có Setter
        return User.builder()
                .id(jpaEntity.getId())
                .username(jpaEntity.getUsername())
                .email(jpaEntity.getEmail())
                .password(jpaEntity.getPassword())
                .role(jpaEntity.getRole())
                .imageUrl(jpaEntity.getImageUrl())
                .build();
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());

        if (domain.getRole() != null) {
            entity.setRole(domain.getRole());
        }
        entity.setImageUrl(domain.getImageUrl());
        return entity;
    }

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getImageUrl());
    }
}