package dienpq.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO (Data Transfer Object) đại diện cho cấu trúc dữ liệu phản hồi API
 * Dashboard Tổng quan.
 * Được thiết kế tối ưu hóa hiệu năng ép kiểu tuần tự (Serialization) JSON.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    // Tổng số lượng sản phẩm có trong hệ thống
    private long totalProducts;

    // Tổng giá trị tài sản kho hàng (Dùng BigDecimal để tránh sai số dấu phẩy động)
    private BigDecimal totalInventoryValue;

    // Số lượng mặt hàng rơi vào trạng thái sắp hết (cảnh báo)
    private long lowStockCount;

    // Số lượng mặt hàng đã hoàn toàn cháy hàng
    private long outOfStockCount;

    // Danh sách chi tiết các sản phẩm bị cảnh báo thiếu hàng (Đã chuyển sang định
    // dạng WebResponse)
    private List<ProductResponse> lowStockProducts;

    /**
     * Dữ liệu thống kê phân phối theo thương hiệu dưới dạng chuỗi JSON thô.
     * Sử dụng @JsonProperty để định dạng chuẩn camelCase đồng bộ với JavaScript
     * Front-End.
     */
    @JsonProperty("brandStatsJson")
    private String brandStatsJson;
}