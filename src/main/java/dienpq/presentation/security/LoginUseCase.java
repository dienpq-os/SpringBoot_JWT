package dienpq.presentation.security;

import dienpq.presentation.dto.AuthResponse;
import dienpq.presentation.dto.LoginRequest;
import dienpq.domain.model.User;
import dienpq.domain.port.external.JwtServicePort;
import dienpq.domain.port.external.PasswordServicePort;
import dienpq.domain.port.repository.UserRepositoryPort;

public class LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordServicePort passwordService;
    private final JwtServicePort jwtService;

    public LoginUseCase(UserRepositoryPort userRepository,
            PasswordServicePort passwordService,
            JwtServicePort jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public AuthResponse execute(LoginRequest request) {
        // 1. Tìm kiếm User theo Username từ cổng lưu trữ của bạn
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác!"));

        // 2. So khớp mật khẩu qua adapter BCrypt (Đã sửa đúng thứ tự raw -> encoded)
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác!");
        }

        // 3. Tạo mã Token JWT từ tên tài khoản bằng JwtServiceAdapter của bạn
        String token = jwtService.generateToken(user.getUsername());

        // 4. Trả về thông tin kết quả sạch
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}