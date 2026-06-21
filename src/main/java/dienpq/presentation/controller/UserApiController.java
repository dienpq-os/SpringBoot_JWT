package dienpq.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import dienpq.application.dto.UserDTO;
import dienpq.application.service.UserAppService;
import dienpq.domain.model.DomainFile;
import dienpq.presentation.dto.ApiResponse;
import dienpq.presentation.dto.UserRequest;
import dienpq.presentation.dto.UserResponse;
import dienpq.presentation.dto.PasswordUpdateRequest;
import dienpq.presentation.dto.ApiErrorResponse;
import dienpq.presentation.mapper.UserWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController // 🌟Sử dụng RestController để trả về JSON thay vì trả về View Name
@RequestMapping("/api/v1/users") // 🌟Định tuyến API nhất quán theo chuẩn RESTful
@RequiredArgsConstructor
public class UserApiController {

    private final UserWebMapper webMapper;
    private final UserAppService userService;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    // 1. DANH SÁCH TÀI KHOẢN (Chỉ dành cho ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> displayList = webMapper.toResponseList(userService.getAllUsers());
        return ResponseEntity.ok(displayList);
    }

    // 2. THÊM USER MỚI
    // (Chỉ dành cho ADMIN - Hỗ trợ nhận dữ liệu Form Data chứa cả File)
    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> saveUser(@Valid @ModelAttribute UserRequest request,
            BindingResult result,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Principal principal) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Dữ liệu đầu vào không hợp lệ.", "errors", result.getAllErrors()));
        }

        try {
            UserDTO userDTO = webMapper.toDTO(request);
            String username = principal.getName();

            userService.create(username, userDTO, toSecureDomainFile(avatar, "/images/users"));
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "✅ Thêm người dùng thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "❌ " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Lỗi hệ thống: Không thể lưu tài khoản."));
        }
    }

    // 3. LẤY CHI TIẾT USER ĐỂ SỬA (Chỉ dành cho ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetail(@PathVariable("id") Integer id) {
        try {
            var userDomain = userService.getUserById(id);
            UserRequest requestForm = webMapper.toRequest(userDomain);
            return ResponseEntity.ok(requestForm);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "❌ Không tìm thấy người dùng."));
        }
    }

    // 4. CẬP NHẬT USER (Chỉ dành cho ADMIN)
    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("id") Integer id,
            @Valid @ModelAttribute UserRequest request,
            BindingResult result,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Principal principal) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu đầu vào không hợp lệ."));
        }

        String username = principal.getName();
        try {
            UserDTO userDTO = webMapper.toDTO(request);
            userService.update(username, userDTO, toSecureDomainFile(avatar, "/images/users"));
            return ResponseEntity.ok(Map.of("message", "✅ Cập nhật thông tin thành công!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Lỗi hệ thống: Không thể cập nhật thông tin tài khoản."));
        }
    }

    // 5. XÓA NGƯỜI DÙNG (Chỉ dành cho ADMIN)
    @DeleteMapping("/{id}") // 🌟 CHUYỂN ĐỔI: Sử dụng DELETE method đúng chuẩn RESTful API thay vì POST
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id, Principal principal) {

        try {
            String username = principal.getName();
            userService.delete(username, id);
            return ResponseEntity.ok(Map.of("message", "✅ Đã xóa người dùng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Không thể xóa người dùng do lỗi hệ thống hoặc phân quyền."));
        }
    }

    // Hàm tiện ích bọc tách Byte an toàn tuyệt đối
    private DomainFile toSecureDomainFile(MultipartFile f, String pathDir) {
        if (f == null || f.isEmpty()) {
            return null;
        }

        if (f.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước tệp tin ảnh vượt quá giới hạn tối đa cho phép (2MB).");
        }

        String contentType = f.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Định dạng tệp tải lên không hợp lệ. Chỉ chấp nhận tệp tin hình ảnh.");
        }

        String originalFilename = StringUtils.cleanPath(f.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Tên tệp tin không hợp lệ. Phát hiện hành vi thao túng đường dẫn.");
        }

        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        if (fileExtension == null || !ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Hệ thống không hỗ trợ phần mở rộng tệp này.");
        }

        String secureFilename = UUID.randomUUID().toString() + "." + fileExtension.toLowerCase();

        try {
            return new DomainFile(
                    pathDir,
                    secureFilename,
                    f.getSize(),
                    f.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể xử lý dữ liệu nhị phân của tệp tin.", e);
        }
    }
}