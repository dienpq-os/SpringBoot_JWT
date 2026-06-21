package dienpq.infrastructure.security;

import dienpq.domain.model.User;
import dienpq.domain.port.external.TokenServicePort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenServicePort tokenServicePort;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(TokenServicePort tokenServicePort, UserDetailsService userDetailsService) {
        this.tokenServicePort = tokenServicePort;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userInput;

        // 1. Cho phép tài nguyên tĩnh, file CSS, JS
        // đi qua tự do không cần kiểm tra Token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7).trim();
            userInput = tokenServicePort.getUsernameFromToken(jwt);

            if (userInput != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userInput);
                // Sử dụng trực tiếp đối tượng userDetails giao diện chuẩn
                // giúp tương thích với mọi loại tài khoản (ADMIN, HANHCHINH, USER)
                String userEmail = userInput;
                // Kiểm tra ép kiểu an toàn
                // tránh ném lỗi ClassCastException làm sập luồng tải file
                // Nếu là CustomUserDetails thì lấy email động,
                // nếu không thì lấy tạm username/identity làm email
                if (userDetails instanceof CustomUserDetails customUser) {
                    userEmail = customUser.getEmail();
                }

                User domainUser = User.builder()
                        .username(userDetails.getUsername())
                        .email(userEmail)
                        .role(userDetails.getAuthorities().stream()
                                .map(auth -> auth.getAuthority())
                                .findFirst()
                                .orElse("USER"))
                        .build();
                // Nếu token hợp lệ, nạp trực tiếp vào SecurityContext chuẩn của Spring
                if (tokenServicePort.validateToken(jwt, domainUser)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, // Sử dụng userDetails chuẩn thay vì ép kiểu ép buộc
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            this.logger.error("Lỗi xác thực chữ ký JWT: " + e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}