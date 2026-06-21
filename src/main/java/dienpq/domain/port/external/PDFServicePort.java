package dienpq.domain.port.external;

import dienpq.domain.model.Product;
import java.io.OutputStream;
import java.util.List;

public interface PDFServicePort {
    // Tầng in ấn chỉ cần nhận DTO giao diện đã được định dạng hiển thị sạch sẽ
    void exportLowStockReport(List<Product> products, OutputStream outputStream);
}