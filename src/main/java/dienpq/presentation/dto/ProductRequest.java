package dienpq.presentation.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    // Giới hạn độ dài và định dạng:
    // Chỉ cho phép chữ, số, dấu gạch ngang, gạch dưới để chống SQLi/Path Traversal
    @Size(min = 3, max = 50, message = "Mã sản phẩm phải từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-]+$", message = "Mã sản phẩm chỉ được chứa chữ cái, chữ số, dấu gạch ngang (-) và gạch dưới (_)")
    private String maSP;

    @NotBlank(message = "Tên model không được để trống")
    @Size(max = 255, message = "Tên model không được vượt quá 255 ký tự")
    // Phòng chống XSS:
    // Không cho phép chứa các ký tự điều hướng HTML nhạy cảm như <, >, &, ", '
    @Pattern(regexp = "^[^<>\"'&]*$", message = "Tên model không được chứa các ký tự đặc biệt nguy hiểm (< > \" ' &)")
    private String tenModel;

    @NotBlank(message = "Hãng sản xuất không được để trống")
    @Size(max = 100, message = "Tên hãng sản xuất không được vượt quá 100 ký tự")
    // Phòng chống XSS bảo vệ dữ liệu văn bản thuần túy
    @Pattern(regexp = "^[^<>\"'&]*$", message = "Hãng sản xuất không được chứa các ký tự đặc biệt nguy hiểm (< > \" ' &)")
    private String hangSanXuat;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán không được nhỏ hơn 0")
    // Chống lỗi tràn số DB:
    // Giới hạn tối đa 12 chữ số phần nguyên và 2 chữ số phần thập phân
    @Digits(integer = 12, fraction = 2, message = "Giá bán không hợp lệ (Tối đa 12 chữ số phần nguyên và 2 chữ số thập phân)")
    private BigDecimal giaBan;

    @Min(value = 0, message = "Số lượng không được nhỏ hơn 0")
    // Chống tấn công DoS/Tràn số: Giới hạn số lượng nhập kho tối đa hợp lý trong
    // thực tế (Ví dụ: 1 triệu sản phẩm)
    @Max(value = 1000000, message = "Số lượng nhập kho không được vượt quá 1,000,000")
    private Integer soLuong;
}