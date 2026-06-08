package dienpq.presentation.controller;

import dienpq.presentation.dto.AuthResponse;
import dienpq.presentation.dto.LoginRequest;
import dienpq.presentation.security.LoginUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = loginUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Trả về lỗi 401 Unauthorized nếu sai tài khoản hoặc mật khẩu
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // Trả về lỗi 500 nếu có sự cố hệ thống
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi hệ thống!");
        }
    }
}