package dienpq.application.dto;

import dienpq.domain.model.Product;

import java.math.BigDecimal;
import java.util.List;
import lombok.Value;

@Value
public class DashboardSummary {
    Long totalProducts;
    BigDecimal totalInventoryValue;
    Long lowStockCount;
    Long outOfStockCount;
    List<Product> lowStockProducts;
    String brandStatsJson; // Đã được xử lý chuyển đổi thành chuỗi JSON sẵn sàng cho UI
}