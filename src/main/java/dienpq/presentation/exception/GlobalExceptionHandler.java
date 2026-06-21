package dienpq.presentation.exception;

import dienpq.presentation.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi đăng nhập sai tài khoản hoặc mật khẩu
    // (Spring Security & RuntimeException)
    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            RuntimeException.class // Bổ sung để bắt lỗi thông báo đăng nhập sai từ Service
    })
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(Exception ex) {
        // Lấy thông báo lỗi động từ hệ thống, nếu không có sẽ dùng chuỗi mặc định
        String message = ex.getMessage() != null ? ex.getMessage() : "Tài khoản hoặc mật khẩu không chính xác.";

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 2. Bắt lỗi phân quyền (Khi không có Role phù hợp nhưng cố truy cập API)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Bạn không có quyền thực hiện hành động này.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 3. Bắt lỗi dữ liệu đầu vào không hợp lệ từ @Valid
    // Hỗ trợ cả @RequestBody (MethodArgumentNotValidException)
    // và @ModelAttribute (BindException)
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        org.springframework.validation.BindingResult bindingResult = null;

        // Sử dụng Pattern Matching của Java hiện đại để trích xuất trực tiếp
        if (ex instanceof MethodArgumentNotValidException validEx) {
            bindingResult = validEx.getBindingResult();
        } else if (ex instanceof BindException bindEx) {
            bindingResult = bindEx.getBindingResult();
        }

        // Kiểm tra an toàn trước khi duyệt lỗi
        if (bindingResult != null) {
            bindingResult.getAllErrors().forEach(error -> {
                if (error instanceof FieldError fieldError) {
                    errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                } else {
                    errors.put(error.getObjectName(), error.getDefaultMessage());
                }
            });
        }

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                errors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 4. Bắt lỗi khi dung lượng file upload vượt quá giới hạn cấu hình
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "File Too Large",
                "Kích thước file tải lên vượt quá giới hạn cho phép.");
        return ResponseEntity.badRequest().body(error);
    }

    // 5. Bắt lỗi tham số không hợp lệ (Ví dụ: lỗi check định dạng file ảnh)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    // 6. Lỗi tầng Business Logic của riêng dự án
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Business Logic Error",
                ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    // 7. Bắt tất cả các lỗi hệ thống không mong muốn còn lại
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(Exception ex) {
        // Nên sử dụng Logger ở đây để theo dõi hệ thống
        // log.error("System Error: ", ex);

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Đã có lỗi hệ thống xảy ra. Vui lòng thử lại sau.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}