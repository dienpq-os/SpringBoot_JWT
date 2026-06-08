package dienpq.presentation.controller;

import dienpq.domain.port.external.JwtServicePort;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class ApiAuthController {
    // Spring tự sinh bean này nếu bạn cấu hình AuthenticationConfiguration
    private final AuthenticationManager authenticationManager;
    private final JwtServicePort jwtService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> apiLogin(@RequestBody LoginRequest request) {
        // Thực hiện xác thực Username/Password thông qua AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Nếu thành công, sinh chuỗi JWT mã hóa trả lại cho ứng dụng Client
        String token = jwtService.generateToken(authentication.getName());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    // Các DTO nội bộ phục vụ API Auth
    @Getter
    @Setter
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Getter
    @RequiredArgsConstructor
    public static class JwtResponse {
        private final String token;
        private final String tokenType = "Bearer";
    }
}