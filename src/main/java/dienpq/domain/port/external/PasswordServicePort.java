package dienpq.domain.port.external;

import org.springframework.security.crypto.password.PasswordEncoder;

public interface PasswordServicePort {

    // Sinh chuỗi mật khẩu ngẫu nhiên với độ dài chỉ định
    String generateRandomPassword(int length);

    // Mã hóa mật khẩu thô thành chuỗi bảo mật
    String encode(String rawPassword);

    boolean matches(String oldPassword, String password);

    PasswordEncoder getTargetEncoder();
}