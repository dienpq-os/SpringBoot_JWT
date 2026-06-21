package dienpq.presentation.dto;

public record ApiResponse<T>(boolean success, String message, T data) {

    // Hàm 1 tham số: Dùng cho các API không cần trả về dữ liệu (như Thêm, Sửa, Xóa)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // Hàm 2 tham số: Dùng cho API Lấy dữ liệu
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
}