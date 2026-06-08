package dienpq.domain.port.external;

public interface EmailServicePort {

    // Gửi mật khẩu mới được khởi tạo đến email người dùng
    void sendPasswordToUser(String email, String rawPassword);
}