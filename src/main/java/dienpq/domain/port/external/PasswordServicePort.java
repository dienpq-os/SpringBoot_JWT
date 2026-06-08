package dienpq.domain.port.external;

public interface PasswordServicePort {

    // Sinh chuỗi mật khẩu ngẫu nhiên với độ dài chỉ định
    String generateRandomPassword(int length);

    // Mã hóa mật khẩu thô thành chuỗi bảo mật
    String encode(String rawPassword);

    boolean matches(String oldPassword, String encodedPassword);

}