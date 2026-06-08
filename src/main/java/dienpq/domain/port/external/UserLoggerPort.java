package dienpq.domain.port.external;

public interface UserLoggerPort {

    void saveLog(String userId, String message);

    // Khai báo triển khai "rỗng" (No Operation) cho mẫu thiết kế Null Object
    // Giúp các UseCase gọi hàm saveLog thoải mái mà không lo bị
    // NullPointerException nếu chưa được tiêm Bean
    UserLoggerPort NOOP = (userId, message) -> {
    };
}