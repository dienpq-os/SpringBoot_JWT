package dienpq.domain.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;
    private String username;
    private String email;
    private String password; // Đây là mật khẩu đã mã hóa (Hashed Password)
    private String role;
    private String imageUrl;

    // Constructor tiện dụng
    // (Bổ sung validate để đảm bảo đối tượng tạo ra luôn sạch)
    public User(String username, String email, String password, String role) {
        validateUsername(username);
        validateEmail(email);

        this.username = username;
        this.email = email;
        this.password = password;
        this.role = (role == null || role.isBlank()) ? "USER" : role.toUpperCase();
    }

    // CÁC HÀM NGHIỆP VỤ TỰ THÂN (DOMAIN LOGIC)
    // Nghiệp vụ 1: Khởi tạo thông tin User mới (Chỉ nhận dữ liệu thô hợp lệ)
    public void initializeNewUser(String username, String email, String role, String encodedPassword, String imageUrl) {
        validateEmail(email);
        validateUsername(username);

        this.username = username;
        this.email = email;
        this.role = (role == null || role.isBlank()) ? "USER" : role.toUpperCase();
        this.password = encodedPassword;
        this.imageUrl = imageUrl;
    }

    // Nghiệp vụ 2: Cập nhật thông tin tài khoản cơ bản
    public void updateProfile(String username, String email, String role) {
        validateEmail(email);
        validateUsername(username);

        this.username = username;
        this.email = email;

        // Không được phép tự ý tước quyền ADMIN cuối cùng (Logic này có thể nâng cấp
        // thêm)
        if ("ADMIN".equals(this.role) && !"ADMIN".equals(role)) {
            throw new IllegalStateException("Không thể hạ quyền của tài khoản Admin hệ thống!");
        }
        this.role = role.toUpperCase();
    }

    // Nghiệp vụ 3: Thay đổi ảnh đại diện
    public void updateAvatar(String newImageUrl) {
        if (newImageUrl != null && !newImageUrl.isBlank()) {
            this.imageUrl = newImageUrl;
        }
    }

    // Nghiệp vụ 4: Thay đổi mật khẩu
    // (Domain tự kiểm tra quy tắc nghiệp vụ về mật khẩu)
    public void changePassword(String newRawPassword, String confirmPassword, String encodedNewPassword) {
        if (newRawPassword == null || newRawPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }
        if (!newRawPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới và mật khẩu xác nhận không khớp!");
        }

        // Chỉ lưu mật khẩu sau khi tầng ngoài đã mã hóa an toàn
        this.password = encodedNewPassword;
    }

    // CÁC PHƯƠNG THỨC KIỂM TRA NỘI BỘ (VALIDATORS)
    private void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Định dạng Email không hợp lệ!");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 3 ký tự!");
        }
    }
}
