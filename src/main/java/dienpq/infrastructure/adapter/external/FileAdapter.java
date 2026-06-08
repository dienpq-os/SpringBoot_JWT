package dienpq.infrastructure.adapter.external;

import dienpq.domain.model.DomainFile;
import dienpq.domain.port.external.FileServicePort;

import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileAdapter implements FileServicePort {

    // Định nghĩa thư mục gốc lưu trữ ngoài (External Root)
    private final String EXTERNAL_STORAGE_ROOT = "uploads";

    @Override
    public String storeFile(DomainFile file) {
        if (file == null || file.getContent() == null || file.getContent().length == 0) {
            return null;
        }

        try {
            // 1. Tạo đường dẫn thư mục đích dựa trên PathDir nghiệp vụ
            Path productUploadDir = Paths.get(EXTERNAL_STORAGE_ROOT, file.getPathDir()).normalize();

            // 2. Tự động khởi tạo thư mục vật lý nếu chưa tồn tại
            if (!Files.exists(productUploadDir)) {
                Files.createDirectories(productUploadDir);
            }

            // 3. Trích xuất đuôi mở rộng
            // và sinh tên tệp tin ngẫu nhiên (UUID) chống trùng lặp
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFilename = UUID.randomUUID().toString() + extension;

            // 4. Định vị tệp tin đích và thực hiện ghi mảng byte đồng bộ xuống đĩa cứng
            Path destinationFile = productUploadDir.resolve(savedFilename).normalize().toAbsolutePath();
            Files.write(destinationFile, file.getContent());

            // 5. Trả về đường dẫn chuỗi sạch dạng tương đối để lưu trữ vào Cơ sở dữ liệu
            return file.getPathDir() + "/" + savedFilename;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi kỹ thuật không thể ghi tệp tin vật lý xuống đĩa: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            // Định vị chính xác tệp tin cần xóa dựa trên chuỗi URL lưu tại DB
            Path path = Paths.get(EXTERNAL_STORAGE_ROOT, imageUrl).normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Chuyển đổi in lỗi sang Log chuẩn hóa nếu dự án của bạn có Logger
            System.err.println("Không thể xóa file ảnh vật lý: " + e.getMessage());
        }
    }
}