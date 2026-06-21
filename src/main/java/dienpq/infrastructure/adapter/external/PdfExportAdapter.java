package dienpq.infrastructure.adapter.external;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import dienpq.domain.model.Product;
import dienpq.domain.port.external.PDFServicePort;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component // Triển khai chi tiết của tầng Kỹ thuật
public class PdfExportAdapter implements PDFServicePort {

    private final ResourceLoader resourceLoader;

    public PdfExportAdapter(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void exportLowStockReport(List<Product> products, OutputStream outputStream) {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/fonts/arial.ttf");
            PdfFont vietnameseFont;

            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    vietnameseFont = PdfFontFactory.createFont(is.readAllBytes(), PdfEncodings.IDENTITY_H);
                }
            } else {
                vietnameseFont = PdfFontFactory.createFont();
            }

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setFont(vietnameseFont);

            document.add(new Paragraph("BÁO CÁO SẢN PHẨM TỒN KHO THẤP")
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));

            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            document.add(new Paragraph("Ngày xuất báo cáo: " + currentTime)
                    .setItalic().setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));

            float[] columnWidths = { 15, 40, 25, 20 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("Mã SP").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Tên Model").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Hãng SX").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Số lượng").setBold()));

            if (products != null && !products.isEmpty()) {
                for (Product p : products) {
                    table.addCell(new Cell().add(new Paragraph(p.getMaSP())));
                    table.addCell(new Cell().add(new Paragraph(p.getTenModel())));
                    table.addCell(new Cell().add(new Paragraph(p.getHangSanXuat())));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getSoLuong()))));
                }
            } else {
                table.addCell(new Cell(1, 4).add(new Paragraph("Không có sản phẩm nào sắp hết hàng.")
                        .setTextAlignment(TextAlignment.CENTER)));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kỹ thuật khi tạo file PDF bằng iText", e);
        }
    }
}