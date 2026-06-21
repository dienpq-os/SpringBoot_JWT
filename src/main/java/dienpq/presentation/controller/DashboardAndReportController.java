package dienpq.presentation.controller;

import dienpq.application.dto.DashboardSummary;
import dienpq.domain.model.Product;
import dienpq.domain.port.external.PDFServicePort;
import dienpq.application.service.DashboardAndReportService;
import dienpq.presentation.dto.DashboardSummaryResponse;
import dienpq.presentation.dto.ProductResponse;
import dienpq.presentation.mapper.ProductWebMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard") // Gom cụm các đường dẫn API chung
public class DashboardAndReportController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardAndReportController.class);

    private final DashboardAndReportService dashboardAndReportService;
    private final PDFServicePort pdfReportExporter;
    private final ProductWebMapper productWebMapper;

    // API RESTful lấy thông tin tổng quan Dashboard.
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummaryApi() {
        logger.info("Yêu cầu REST API: Lấy thông tin thống kê Dashboard");

        DashboardSummary summary = dashboardAndReportService.getDashboardSummary();
        List<ProductResponse> lowStockResponses = productWebMapper.toResponseList(summary.getLowStockProducts());

        // Sử dụng Builder của DTO thay vì khởi tạo HashMap thủ công
        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .totalProducts(summary.getTotalProducts())
                .totalInventoryValue(summary.getTotalInventoryValue())
                .lowStockCount(summary.getLowStockCount())
                .outOfStockCount(summary.getOutOfStockCount())
                .lowStockProducts(lowStockResponses)
                .brandStatsJson(summary.getBrandStatsJson())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * API tải file báo cáo PDF tồn kho.
     * Sử dụng StreamingResponseBody giúp xuất file không bị nghẽn luồng
     * (Non-blocking I/O).
     * Đã gỡ bỏ kiểm tra token thủ công. Hãy cấu hình Spring Security tự
     * động lọc token từ Header/Cookie.
     */
    @GetMapping("/export-pdf")
    public ResponseEntity<StreamingResponseBody> exportToPDF() {
        logger.info("Yêu cầu REST API: Xuất tệp báo cáo PDF tồn kho");

        // Lấy dữ liệu thống kê từ Service trên Luồng Chính (Main Thread)
        DashboardSummary summary = dashboardAndReportService.getDashboardSummary();

        // Ép nạp sẵn dữ liệu các Entity vào RAM ngay trên
        // Luồng Chính bằng hàm .size() công khai
        // Việc này kích hoạt Hibernate nạp dữ liệu lập tức,
        // tránh lỗi mất kết nối DB khi sang luồng phụ
        List<Product> rawProducts = summary.getLowStockProducts();
        if (rawProducts != null) {
            rawProducts.size();
        }

        // Khởi tạo luồng xuất file bất đồng bộ an toàn
        // với danh sách Entity đã nạp đầy đủ
        StreamingResponseBody responseBody = out -> {
            try {
                pdfReportExporter.exportLowStockReport(rawProducts, out);
                out.flush(); // Giải phóng luồng dữ liệu byte ra mạng
                logger.info("Xuất luồng dữ liệu PDF thành công!");
            } catch (Exception e) {
                logger.error("Lỗi khi ghi luồng PDF: {}", e.getMessage(), e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Bao_cao_ton_kho.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(responseBody);
    }
}