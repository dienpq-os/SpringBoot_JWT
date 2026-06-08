package dienpq.infrastructure.adapter.persistence.entity;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dienpq.domain.model.Role;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 500)
    private String imageUrl; // Lưu đường dẫn hoặc tên file ảnh đại diện

    public String getRole() {
        return role != null ? role.name() : "";
    }

    public void setRole(String roleName) {
        if (roleName != null) {
            try {
                // Xử lý cả trường hợp truyền vào "ADMIN" hoặc "ROLE_ADMIN"
                this.role = Role.valueOf(roleName.replace("ROLE_", "").trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                this.role = null;
            }
        }
    }

    // --- Spring Security Methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null)
            return Collections.emptyList();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}