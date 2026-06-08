package dienpq.infrastructure.adapter.external;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import dienpq.domain.port.external.PasswordServicePort;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class BCryptPasswordAdapter implements PasswordServicePort {

    // NÂNG CẤP: Sử dụng độ mạnh 12 (Log Rounds = 12) giúp chống tấn công vét cạn
    // bằng GPU hiệu quả hơn
    // Sử dụng chung một instance SecureRandom để tối ưu hóa tài nguyên hệ thống
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12, SECURE_RANDOM);

    // Định nghĩa các nhóm ký tự phục vụ sinh mật khẩu mạnh
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "@#$%^&*_-";
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;

    @Override
    public boolean matches(String oldPassword, String encodedPassword) {
        return passwordEncoder.matches(oldPassword, encodedPassword);
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * TỐI ƯU: Thuật toán sinh mật khẩu ngẫu nhiên đáp ứng nghiêm ngặt quy tắc bảo
     * mật (Password Policy)
     * Đảm bảo mật khẩu sinh ra LUÔN CÓ ít nhất: 1 chữ thường, 1 chữ hoa, 1 số, 1 ký
     * tự đặc biệt.
     */
    @Override
    public String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Độ dài mật khẩu bảo mật tối thiểu phải từ 8 ký tự trở lên.");
        }

        List<Character> passwordChars = new ArrayList<>();

        // 1. Ép buộc lấy ít nhất 1 ký tự từ mỗi nhóm bắt buộc để đảm bảo độ phức tạp
        passwordChars.add(CHAR_LOWER.charAt(SECURE_RANDOM.nextInt(CHAR_LOWER.length())));
        passwordChars.add(CHAR_UPPER.charAt(SECURE_RANDOM.nextInt(CHAR_UPPER.length())));
        passwordChars.add(NUMBER.charAt(SECURE_RANDOM.nextInt(NUMBER.length())));
        passwordChars.add(OTHER_CHAR.charAt(SECURE_RANDOM.nextInt(OTHER_CHAR.length())));

        // 2. Điền đầy các vị trí còn lại bằng tập hợp tất cả các ký tự cho phép
        for (int i = 4; i < length; i++) {
            passwordChars.add(PASSWORD_ALLOW_BASE.charAt(SECURE_RANDOM.nextInt(PASSWORD_ALLOW_BASE.length())));
        }

        // 3. Trộn đều danh sách ký tự để kẻ tấn công không đoán được vị trí cấu trúc cố
        // định
        Collections.shuffle(passwordChars, SECURE_RANDOM);

        // 4. Dựng lại chuỗi mật khẩu hoàn chỉnh
        StringBuilder password = new StringBuilder(length);
        for (char c : passwordChars) {
            password.append(c);
        }
        return password.toString();
    }

}