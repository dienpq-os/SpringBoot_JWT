package dienpq.presentation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Kích hoạt phân quyền @PreAuthorize cho API
@RequiredArgsConstructor
@Order(1) // Ép buộc Spring Security phải kiểm tra chuỗi lọc API này ĐẦU TIÊN
public class ApiSecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                // QUAN TRỌNG: Chỉ áp dụng file cấu hình này cho các URL bắt đầu bằng /api/
                                .securityMatcher("/api/v1/**")

                                .authorizeHttpRequests(auth -> auth
                                                // Cho phép gọi API đăng nhập công khai để lấy Token JWT
                                                .requestMatchers("/api/v1/auth/login").permitAll()
                                                // Các chức năng API khác của sản phẩm bắt buộc phải check quyền token
                                                .requestMatchers("/api/v1/products/**").hasAnyRole("HANHCHINH", "ADMIN")
                                                .anyRequest().authenticated())

                                // Vì API sử dụng JWT Token độc lập gửi qua Header
                                // nên TẮT hoàn toàn CSRF an toàn
                                .csrf(csrf -> csrf.disable())

                                // Khóa chặt: Không tạo hay lưu bất kỳ Session nào trên RAM của Server
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Đánh chặn và kiểm tra chuỗi Token trước khi Spring Security xử lý định danh
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}