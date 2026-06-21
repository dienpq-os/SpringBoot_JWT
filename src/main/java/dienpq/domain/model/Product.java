package dienpq.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String maSP;
    private String tenModel;
    private String hangSanXuat;
    private BigDecimal giaBan;
    private Integer soLuong;

    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    public Product(String maSP, String tenModel, String hangSanXuat, BigDecimal giaBan, Integer soLuong) {
        validateDetails(giaBan, soLuong);
        this.maSP = maSP;
        this.tenModel = tenModel;
        this.hangSanXuat = hangSanXuat;
        this.giaBan = giaBan;
        this.soLuong = soLuong;
    }

    // Đóng gói danh sách ảnh,
    // không cho phép bên ngoài dùng .getImages().add() bừa bãi
    public List<ProductImage> getImages() {
        if (this.images == null)
            return new ArrayList<>();
        return Collections.unmodifiableList(this.images);
    }

    // Tách riêng hàm validate để tái sử dụng
    private void validateDetails(BigDecimal giaBan, Integer soLuong) {
        if (giaBan == null || giaBan.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bán không được phép âm hoặc rỗng!");
        }
        if (soLuong == null || soLuong < 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm không được nhỏ hơn 0!");
        }
    }

    // Cập nhật thông tin cơ bản
    public void updateDetails(String tenModel, String hangSanXuat, BigDecimal giaBan, int soLuong) {
        validateDetails(giaBan, soLuong);
        this.tenModel = tenModel;
        this.hangSanXuat = hangSanXuat;
        this.giaBan = giaBan;
        this.soLuong = soLuong;
    }

    // Lọc tìm các đường dẫn ảnh cũ cần xóa vật lý
    public List<String> getUrlsByImageIds(List<Long> deleteImageIds) {
        if (deleteImageIds == null || deleteImageIds.isEmpty() || this.images == null) {
            return Collections.emptyList();
        }
        return this.images.stream()
                .filter(img -> deleteImageIds.contains(img.getId()))
                .map(ProductImage::getImageUrl)
                .toList();
    }

    // Đồng bộ trạng thái ảnh và định vị ảnh chính an toàn
    public void updateImagesAndMainStatus(List<Long> deleteImageIds, List<String> newImageUrls, String mainImageId) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        } else {
            // Chuyển sang ArrayList bọc ngoài nếu danh sách hiện tại bất biến
            this.images = new ArrayList<>(this.images);
        }

        // 1. Xóa ảnh cũ
        if (deleteImageIds != null) {
            this.images.removeIf(img -> deleteImageIds.contains(img.getId()));
        }

        // 2. Thêm ảnh mới
        if (newImageUrls != null) {
            for (String url : newImageUrls) {
                this.images.add(ProductImage.builder().imageUrl(url).isMain(false).build());
            }
        }

        // 3. Reset toàn bộ ảnh về false trước khi thiết lập ảnh chính mới
        this.images.forEach(img -> img.setMain(false));

        // 4. Định vị ảnh chính dựa trên cơ chế định danh an toàn
        if (mainImageId != null && !this.images.isEmpty()) {
            if (mainImageId.startsWith("new_")) {
                try {
                    int newImgIndex = Integer.parseInt(mainImageId.replace("new_", ""));
                    int currentFileCounter = 0;
                    for (ProductImage img : this.images) {
                        if (img.getId() == null) { // Nhận diện ảnh mới thêm chưa có ID DB
                            if (currentFileCounter == newImgIndex) {
                                img.setMain(true);
                                break;
                            }
                            currentFileCounter++;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Không dùng System.err,
                    // ném exception rõ nghĩa để UseCase bắt hoặc GlobalHandler xử lý
                    throw new IllegalArgumentException("Định dạng chỉ mục ảnh mới 'new_' không hợp lệ!", e);
                }
            } else {
                try {
                    Long targetId = Long.parseLong(mainImageId);
                    this.images.stream()
                            .filter(img -> Objects.equals(img.getId(), targetId))
                            .findFirst()
                            .ifPresent(img -> img.setMain(true));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Định dạng ID ảnh cũ không hợp lệ!", e);
                }
            }
        }

        ensureAtLeastOneMainImage();
    }

    // Khởi tạo cụm ảnh mới cho sản phẩm vừa tạo
    public void initializeImages(List<String> uploadedUrls, int mainImageIndex) {
        this.images = new ArrayList<>();
        if (uploadedUrls != null && !uploadedUrls.isEmpty()) {
            for (int i = 0; i < uploadedUrls.size(); i++) {
                this.images.add(ProductImage.builder()
                        .imageUrl(uploadedUrls.get(i))
                        .isMain(i == mainImageIndex)
                        .build());
            }
        }
        ensureAtLeastOneMainImage();
    }

    public List<String> getAllImageUrls() {
        if (this.images == null)
            return Collections.emptyList();
        return this.images.stream().map(ProductImage::getImageUrl).toList();
    }

    // Trả về chuỗi rỗng hoặc null, việc hiển thị ảnh mặc định là trách nhiệm của
    // tầng Presentation (Thẻ <img> của HTML hoặc logic xử lý DTO Web).
    public String getMainImageUrl() {
        if (this.images == null || this.images.isEmpty()) {
            return "";
        }
        return this.images.stream()
                .filter(ProductImage::isMain)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(this.images.get(0).getImageUrl());
    }

    private void ensureAtLeastOneMainImage() {
        if (this.images != null && !this.images.isEmpty()) {
            boolean hasMain = this.images.stream().anyMatch(ProductImage::isMain);
            if (!hasMain) {
                this.images.get(0).setMain(true);
            }
        }
    }
}