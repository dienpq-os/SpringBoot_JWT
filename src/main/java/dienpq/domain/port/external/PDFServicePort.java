package dienpq.domain.port.external;

import dienpq.presentation.dto.ProductResponse; // Import DTO tầng giao diện
import java.io.OutputStream;
import java.util.List;

public interface PDFServicePort {
    // Tầng in ấn chỉ cần nhận DTO giao diện đã được định dạng hiển thị sạch sẽ
    void exportLowStockReport(List<ProductResponse> products, OutputStream outputStream);
}