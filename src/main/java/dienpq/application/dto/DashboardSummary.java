package dienpq.application.dto;

import dienpq.domain.model.Product;
import java.util.List;
import lombok.Value;

@Value
public class DashboardSummary {
    long totalProducts;
    double totalInventoryValue;
    long lowStockCount;
    long outOfStockCount;
    List<Product> lowStockProducts;
    String brandStatsJson; // Đã được xử lý chuyển đổi thành chuỗi JSON sẵn sàng cho UI
}