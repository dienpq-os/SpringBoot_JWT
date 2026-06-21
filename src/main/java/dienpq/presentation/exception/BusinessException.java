package dienpq.presentation.exception;

// Định nghĩa class lỗi nghiệp vụ riêng cho dự án
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
