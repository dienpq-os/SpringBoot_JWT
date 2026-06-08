package dienpq.application.service;

import dienpq.application.dto.DashboardSummary;
import dienpq.domain.model.Product;
import dienpq.domain.port.external.JsonSerializerPort;
import dienpq.domain.port.repository.ProductRepositoryPort;
import java.util.Map;
import java.util.List;

import lombok.RequiredArgsConstructor;

@MyBean
@RequiredArgsConstructor
public class DashboardAndReportServiceImpl implements DashboardAndReportService {
    private final ProductRepositoryPort productRepositoryPort;
    private final JsonSerializerPort jsonSerializerPort;

    @Override
    public DashboardSummary getDashboardSummary() {
        long totalProducts = productRepositoryPort.countTotal();
        double totalInventoryValue = productRepositoryPort.sumTotalInventoryValue();
        long lowStockCount = productRepositoryPort.countLowStock(10);
        long outOfStockCount = productRepositoryPort.countOutOfStock();
        List<Product> lowStockProducts = productRepositoryPort.findBySoLuongLessThan(10);

        Map<String, Long> brandStats = productRepositoryPort.getCountByBrand();
        String brandStatsJson = jsonSerializerPort.toJson(brandStats);

        return new DashboardSummary(totalProducts, totalInventoryValue, lowStockCount,
                outOfStockCount, lowStockProducts, brandStatsJson);
    }

}
