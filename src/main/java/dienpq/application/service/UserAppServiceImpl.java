package dienpq.application.service;

import dienpq.application.dto.LoginRequest;
import dienpq.application.dto.LoginResponse;
import dienpq.application.dto.UserDTO;
import dienpq.application.utils.ResourceRollbackHook;
import dienpq.domain.model.DomainFile;
import dienpq.domain.model.JwtToken;
import dienpq.domain.model.User;
import dienpq.domain.port.external.EmailServicePort;
import dienpq.domain.port.external.FileServicePort;
import dienpq.domain.port.external.PasswordServicePort;
import dienpq.domain.port.external.TokenServicePort;
import dienpq.domain.port.external.UserLoggerPort;
import dienpq.domain.port.repository.UserRepositoryPort;

import java.util.List;
import java.util.NoSuchElementException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@MyBean
@RequiredArgsConstructor
public class UserAppServiceImpl implements UserAppService {
    private final UserRepositoryPort userRepositoryPort;
    private final FileServicePort fileServicePort;
    private final PasswordServicePort passwordServicePort;
    private final EmailServicePort emailServicePort;
    private final UserLoggerPort userLoggerPort;
    private final TokenServicePort tokenServicePort;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. Phục hồi dữ liệu an toàn phòng trường hợp Frontend gửi sai tên thuộc tính
        // DTO
        String identity = request.getUsername();

        // Nếu biến identity bị null, hệ thống ném lỗi tường minh ngay tại đây
        // thay vì đẩy xuống tầng DB sinh câu lệnh SQL lỗi "IS NULL"
        if (identity == null || identity.trim().isEmpty()) {
            throw new RuntimeException(
                    "Dữ liệu đăng nhập (Email/Username) gửi lên bị rỗng. Hãy kiểm tra lại tên thuộc tính JSON từ Frontend!");
        }

        // 2. Tìm kiếm user từ DB thông qua Port kép
        User user = userRepositoryPort.findByEmailOrUsername(identity, identity)
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không chính xác"));

        // 3. Kiểm tra mật khẩu thông qua Password Port
        if (!passwordServicePort.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không chính xác");
        }

        // 4. Sinh mã JWT Token
        JwtToken jwtToken = tokenServicePort.generateToken(user);

        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .imageUrl(user.getImageUrl())
                .build();

        LoginResponse response = new LoginResponse(jwtToken, dto);
        return response;
    }

    @Override
    public void changePassword(String usernameOrEmail, String oldPassword, String newPassword, String confirmPassword) {
        User user = userRepositoryPort.findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng: " + usernameOrEmail));

        // Tầng ứng dụng chịu trách nhiệm phối hợp với
        // PasswordEncoder hạ tầng để khớp pass cũ
        if (!passwordServicePort.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác!");
        }

        // Mã hóa mật khẩu mới trước khi đẩy xuống cho Domain kiểm duyệt
        String encodedNewPassword = passwordServicePort.encode(newPassword);

        // Giao quyền kiểm tra độ dài và sự trùng khớp cho Domain xử lý
        user.changePassword(newPassword, confirmPassword, encodedNewPassword);

        userRepositoryPort.save(user);
        userLoggerPort.saveLog(usernameOrEmail, "Đổi mật khẩu thành công");
    }

    @Override
    public User create(String userName, UserDTO dto, DomainFile avatar) throws IOException {
        if (userRepositoryPort.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + dto.getEmail());
        }
        if (userRepositoryPort.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại: " + dto.getUsername());
        }

        // 1. Tầng dịch vụ giải quyết các yếu tố kỹ thuật ngoài hạ tầng trước
        String imageUrl = fileServicePort.storeFile(avatar);
        String rawPassword = passwordServicePort.generateRandomPassword(10);
        String encodedPassword = passwordServicePort.encode(rawPassword);

        // 2. Gọi hàm dựng của Rich Domain để tự kiểm tra dữ liệu và khởi tạo trạng thái
        User user = new User(dto.getUsername(), dto.getEmail(), encodedPassword, dto.getRole());
        user.updateAvatar(imageUrl);

        // 3. Thực hiện lưu vết và kích hoạt ResourceRollbackHook mới
        // Thứ tự tham số thay đổi: (Hàm xóa trước, sau đó là danh sách URL)
        try (ResourceRollbackHook rollbackHook = new ResourceRollbackHook(fileServicePort::deleteFile, imageUrl)) {
            User savedUser = userRepositoryPort.save(user);
            rollbackHook.commit();
            userLoggerPort.saveLog(userName, "Tạo user mới thành công: " + savedUser.getUsername());
            // Gửi mail sau khi hệ thống lưu cơ sở dữ liệu thành công
            emailServicePort.sendPasswordToUser(savedUser.getEmail(), rawPassword);
            return savedUser;
        }
    }

    @Override
    public void delete(String userName, Integer id) {
        User user = userRepositoryPort.findById(id).get();
        userRepositoryPort.deleteById(id);
        if (user.getImageUrl() != null) {
            fileServicePort.deleteFile(user.getImageUrl());
            userLoggerPort.saveLog(userName, "Xóa user thành công: " + user.getUsername());
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepositoryPort.findAll();
    }

    @Override
    public User getUserById(Integer id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    public User getUserByIdentity(String identity) {
        return userRepositoryPort.findByEmailOrUsername(identity, identity)
                .orElseThrow(
                        () -> new NoSuchElementException("❌ Không tìm thấy người dùng với danh tính: " + identity));
    }

    @Override
    public User update(String userName, UserDTO dto, DomainFile avatar) throws IOException {
        User existing = userRepositoryPort.findById(dto.getId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với ID: " + dto.getId()));

        // Thay vì dùng setter thủ công, uỷ quyền kiểm tra logic cho Domain Entity
        existing.updateProfile(dto.getUsername(), dto.getEmail(), dto.getRole());

        String newImageUrl = null;
        String oldImageUrl = existing.getImageUrl();

        if (avatar != null) {
            newImageUrl = fileServicePort.storeFile(avatar);
            existing.updateAvatar(newImageUrl); // Đẩy đường dẫn ảnh mới vào Domain
        }

        // Kích hoạt ResourceRollbackHook mới bảo vệ tiến trình cập nhật ảnh mới
        // Cú pháp Varargs tự động xử lý mảng (dù truyền 1 biến `newImageUrl` độc lập)
        try (ResourceRollbackHook rollbackHook = new ResourceRollbackHook(fileServicePort::deleteFile, newImageUrl)) {
            User updatedUser = userRepositoryPort.save(existing);
            rollbackHook.commit();

            if (newImageUrl != null && oldImageUrl != null) {
                fileServicePort.deleteFile(oldImageUrl);
            }
            userLoggerPort.saveLog(userName, "Cập nhật người dùng thành công : " + updatedUser.getId());
            return updatedUser;
        }
    }
}