package dienpq.application.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Size(min = 3, max = 20, message = "Mã sản phẩm phải từ 3 đến 20 ký tự")
    private String maSP;

    @NotBlank(message = "Tên model không được để trống")
    @Size(min = 2, max = 100, message = "Tên model phải từ 2 đến 100 ký tự")
    private String tenModel;

    @NotBlank(message = "Hãng sản xuất không được để trống")
    private String hangSanXuat;

    // Sử dụng Double (Wrapper) để @NotNull có thể bắt được trường hợp bỏ trống ô
    // nhập liệu
    @NotNull(message = "Giá bán không được để trống")
    @Positive(message = "Giá bán phải lớn hơn 0")
    private BigDecimal giaBan;

    @Min(value = 0, message = "Số lượng không được âm")
    private int soLuong;

    // Danh sách đường dẫn ảnh để hiển thị trên giao diện
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    // Ảnh chính để hiển thị làm đại diện
    private String mainImageUrl;
}
