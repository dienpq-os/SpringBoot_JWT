package dienpq.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    private Long id;
    private String imageUrl;
    private boolean isMain;

    // Thay thế @Setter bằng một package-private hoặc public setter có kiểm soát
    // Cho phép lớp Product cùng package (dienpq.domain.model) cấu hình trạng thái
    public void setMain(boolean isMain) {
        this.isMain = isMain;
    }

    // Nếu hạ tầng (như Mapper) cần gán ID sau khi lưu DB, tạo hàm gán ID an toàn
    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Không thể thay đổi ID của ảnh khi đã được gán!");
        }
        this.id = id;
    }
}