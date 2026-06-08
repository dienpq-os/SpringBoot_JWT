package dienpq.application.service;

import dienpq.application.dto.ProductDTO;
import dienpq.application.utils.ResourceRollbackHook;
import dienpq.domain.model.DomainFile;
import dienpq.domain.model.PagedResult;
import dienpq.domain.model.Product;
import dienpq.domain.port.external.FileServicePort;
import dienpq.domain.port.external.TransactionManagerPort;
import dienpq.domain.port.external.UserLoggerPort;
import dienpq.domain.port.repository.ProductRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@MyBean
@RequiredArgsConstructor
public class ProductAppServiceImpl implements ProductAppService {
    private final FileServicePort storageService;
    private final TransactionManagerPort transactionManager;
    private final UserLoggerPort userLogger;
    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public PagedResult<Product> listPagedResult(String keyword, int page, int size) {
        return productRepositoryPort.searchProductsPaginated(keyword, page, size);
    }

    @Override
    public Product getProductById(String maSP) {
        return productRepositoryPort.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Mã SP không tồn tại: " + maSP));
    }

    @Override
    public double getInventoryValue() {
        return productRepositoryPort.sumTotalInventoryValue();
    }

    @Override
    public void delete(String maSP, String username) {
        // 1. KIỂM TRA TRƯỚC (Ngoài Transaction):
        // Tìm kiếm sản phẩm và trích xuất danh sách ảnh cũ
        Product product = productRepositoryPort.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm mã: " + maSP));

        List<String> finalUrls = product.getAllImageUrls();

        // 2. KÍCH HOẠT GIAO DỊCH DB CHỚP NHOÁNG:
        transactionManager.runInTransaction(() -> {
            productRepositoryPort.deleteById(maSP);
        });

        // 3. VẬN HÀNH HẠ TẦNG (Ngoài Transaction):
        // DB đã xóa thành công -> Tiến hành dọn sạch kho ảnh vật lý
        if (finalUrls != null) {
            for (String url : finalUrls) {
                try {
                    storageService.deleteFile(url);
                } catch (Exception e) {
                    // Đẩy lỗi hạ tầng dọn file vào hệ thống log chính quy
                    userLogger.saveLog(username, "⚠️ LỖI HẠ TẦNG: Không thể xóa file vật lý ["
                            + url + "] khi xóa sản phẩm: " + e.getMessage());
                }
            }
        }
        // 4. KIỂM TOÁN NGHIỆP VỤ:
        userLogger.saveLog(username, "🗑️ Đã xóa thành công sản phẩm có mã: " + maSP);
    }

    @Override
    public Product save(ProductDTO dto, List<DomainFile> images, int mainImageIndex, String username) {
        // Đưa kiểm tra tồn tại vào khối kiểm soát chung để xử lý lỗi đồng bộ
        if (productRepositoryPort.existsById(dto.getMaSP())) {
            throw new IllegalArgumentException("Mã sản phẩm này đã tồn tại trong hệ thống: " + dto.getMaSP());
        }
        List<String> uploadedUrls = new ArrayList<>();

        // BƯỚC 1: VẬN HÀNH HẠ TẦNG (Tải ảnh vật lý ngoài Transaction)
        if (images != null && !images.isEmpty()) {
            for (DomainFile file : images) {
                if (file != null && file.getSize() > 0) {
                    String uploadedUrl = storageService.storeFile(file);
                    uploadedUrls.add(uploadedUrl);
                }
            }
        }

        // BƯỚC 2: KHỞI TẠO BỘ HOÀN TÁC ĐA TỆP TIN (Chuyển List sang mảng String[])
        try (ResourceRollbackHook rollbackHook = new ResourceRollbackHook(
                storageService::deleteFile,
                uploadedUrls.toArray(String[]::new))) {

            // BƯỚC 3: KHỞI TẠO DOMAIN MODEL QUA CONSTRUCTOR CHÍNH CHỦ
            Product product = new Product(
                    dto.getMaSP(),
                    dto.getTenModel(),
                    dto.getHangSanXuat(),
                    dto.getGiaBan(),
                    dto.getSoLuong());

            // Ủy quyền nghiệp vụ gán ảnh và kiểm tra ảnh chính
            product.initializeImages(uploadedUrls, mainImageIndex);

            // BƯỚC 4: KÍCH HOẠT GIAO DỊCH DB
            Product savedProduct;
            try {
                savedProduct = transactionManager.executeInTransaction(() -> {
                    if (productRepositoryPort.existsById(product.getMaSP())) {
                        throw new IllegalArgumentException("Mã sản phẩm đã bị trùng lặp bởi một phiên làm việc khác!");
                    }
                    return productRepositoryPort.save(product);
                });
            } catch (Exception dbException) {
                userLogger.saveLog(username,
                        "❌ Lỗi lưu Database sản phẩm " + product.getMaSP() + ": " + dbException.getMessage());
                throw dbException;
            }

            rollbackHook.commit(); // Thành công -> Hủy lệnh xóa ảnh tạm vật lý
            userLogger.saveLog(username, "📦 Đã thêm mới thành công sản phẩm mã: " + dto.getMaSP());

            return savedProduct;
        }
    }

    @Override
    public void update(String maSP, ProductDTO dto, List<DomainFile> images,
            List<Long> deleteImageIds, String mainImageId, String username) {
        Product product = productRepositoryPort.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại với mã: " + maSP));

        List<String> filesToDeletePhysical = product.getUrlsByImageIds(deleteImageIds);
        List<String> newlyUploadedUrls = new ArrayList<>();

        // VẬN HÀNH HẠ TẦNG (Ngoài Transaction):
        // Upload các file ảnh mới lên đĩa cứng trước
        if (images != null) {
            for (DomainFile file : images) {
                if (file != null && file.getSize() > 0) {
                    newlyUploadedUrls.add(storageService.storeFile(file));
                }
            }
        }

        // Khởi tạo bộ hoàn tác bảo vệ danh sách ảnh MỚI vừa tải lên
        try (ResourceRollbackHook rollbackHook = new ResourceRollbackHook(
                storageService::deleteFile,
                newlyUploadedUrls.toArray(String[]::new))) {

            transactionManager.runInTransaction(() -> {
                // Ép lõi nghiệp vụ Domain tự tính toán mảng trạng thái mới trong bộ nhớ RAM
                product.updateDetails(dto.getTenModel(), dto.getHangSanXuat(), dto.getGiaBan(), dto.getSoLuong());
                product.updateImagesAndMainStatus(deleteImageIds, newlyUploadedUrls, mainImageId);
                // Lưu trạng thái mới xuống DB
                productRepositoryPort.save(product);
            });

            rollbackHook.commit(); // DB thành công -> Giữ ảnh mới
            userLogger.saveLog(username, "📝 Đã cập nhật thành công thông tin sản phẩm mã: " + maSP);

            // DỌN DẸP HẠ TẦNG (Xóa ảnh CŨ):
            // Chỉ xóa ảnh cũ khi DB đã cập nhật thành công thông tin ảnh mới
            filesToDeletePhysical.forEach(storageService::deleteFile);
        }
    }
}