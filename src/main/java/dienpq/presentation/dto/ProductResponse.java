package dienpq.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String maSP;
    private String tenModel;
    private String hangSanXuat;
    private BigDecimal giaBan;
    private Integer soLuong;

    // Đường dẫn ảnh chính để hiển thị trực tiếp trên danh sách
    private String mainImageUrl;

    // Danh sách chi tiết cấu trúc ảnh nếu giao diện cần hiển thị slider/album
    private List<ProductImageResponse> images;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageResponse {
        private Long id;
        private String imageUrl;
        private boolean isMain;
    }
}
