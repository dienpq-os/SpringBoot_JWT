package dienpq.presentation.controller;

import dienpq.application.dto.LoginResponse;
import dienpq.presentation.dto.ApiResponse;
import dienpq.presentation.dto.ApiErrorResponse;
import dienpq.presentation.dto.AuthRequest;
import dienpq.presentation.dto.PasswordUpdateRequest;
import dienpq.presentation.dto.UserResponse;
import dienpq.application.service.UserAppService;
import dienpq.presentation.mapper.UserWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

// REST Controller đóng vai trò là Adapter đầu vào (Primary/Driving Adapter)
// Phục vụ các yêu cầu xác thực người dùng từ Client.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthenticationController {

    private final UserAppService userAppService;
    private final UserWebMapper userMapper;

    /**
     * Endpoint xử lý yêu cầu đăng nhập hệ thống.
     * URL: POST /api/v1/auth/login
     * 
     * @param loginRequest Chứa dữ liệu email/username và password từ client gửi lên
     * @return ResponseEntity trả về HTTP Status 200 kèm LoginResponse chứa JWT
     *         Token nếu thành công
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest authRequest) {
        // Ủy nhiệm toàn bộ logic xử lý xác thực cho Application Service core
        LoginResponse loginResponse = userAppService.login(userMapper.toLoginRequest(authRequest));

        // Trả về kết quả cho Client dưới định dạng JSON
        return ResponseEntity.ok(loginResponse);
    }

    // Lấy về thông tin user đăng nhập
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile(Principal principal) {
        // 1. Lấy thông tin user từ database thông qua identity (email)
        UserResponse user = userMapper.toResponse(userAppService.getUserByIdentity(principal.getName()));

        // 2. Trả về cấu trúc ApiResponse chuẩn hóa chứa dữ liệu UserDTO
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công.", user));
    }

    // ĐỔI MẬT KHẨU USER (Dành cho chính User đang đăng nhập)
    @PostMapping("/update-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateRequest request,
            Principal principal) {
        try {
            userAppService.changePassword(
                    principal.getName(),
                    request.getOldPassword(),
                    request.getNewPassword(),
                    request.getConfirmPassword());

            // Thành công: Dùng ApiResponse record của bạn (HTTP 200)
            return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công."));

        } catch (IllegalArgumentException e) {
            // Lỗi logic/Validate: Dùng ApiErrorResponse record mới (HTTP 400)
            ApiErrorResponse errorBody = new ApiErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    e.getMessage() // Chuỗi String lỗi đơn lẻ
            );
            return ResponseEntity.badRequest().body(errorBody);

        } catch (Exception e) {
            // Lỗi hệ thống: Dùng ApiErrorResponse record mới (HTTP 500)
            ApiErrorResponse errorBody = new ApiErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "Hệ thống đổi mật khẩu gặp sự cố. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

}