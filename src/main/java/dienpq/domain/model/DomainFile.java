package dienpq.domain.model;

import lombok.Value;

/**
 * Đối tượng bọc dữ liệu File thô thuần nghiệp vụ (Domain Model).
 * Đạt trạng thái Immutable tuyệt đối và an toàn bộ nhớ.
 */
@Value
public class DomainFile {
    String pathDir; // Thư mục chứa file (Ví dụ: /images/products)
    String originalFilename; // Tên gốc của tệp tin (Ví dụ: iphone15.jpg)
    long size; // Dung lượng file tính bằng bytes
    byte[] content; // Dữ liệu nhị phân thô (Thay thế hoàn hảo cho InputStream)

    // Hàm tiện ích để kiểm tra file trống an toàn
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }
}