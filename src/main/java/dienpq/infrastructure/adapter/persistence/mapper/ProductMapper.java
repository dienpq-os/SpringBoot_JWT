package dienpq.infrastructure.adapter.persistence.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import dienpq.application.dto.ProductDTO;
import dienpq.domain.model.Product;
import dienpq.domain.model.ProductImage;
import dienpq.infrastructure.adapter.persistence.entity.ProductEntity;
import dienpq.infrastructure.adapter.persistence.entity.ProductImageEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    // 1. CHUYỂN ĐỔI: Entity (DB) -> Domain Model (Lõi)
    // MapStruct tự động dùng Product.builder() để dựng đối tượng vô cùng an toàn
    Product toDomain(ProductEntity entity);

    // ĐÃ SỬA: Sử dụng thuộc tính 'main' từ ProductImageEntity map vào trường
    // 'isMain' của Domain Builder
    // Lưu ý: MapStruct sẽ tự tìm phương thức .isMain() trên Builder của
    // ProductImage
    @Mapping(target = "id", source = "id")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "isMain", source = "main")
    ProductImage toDomainImage(ProductImageEntity entityImage);

    // 2. CHUYỂN ĐỔI: Domain Model -> Entity (DB)
    @Mapping(target = "images", ignore = true) // Bỏ qua để xử lý AfterMapping thủ công thiết lập khóa ngoại
    ProductEntity toEntity(Product domain);

    // ĐÃ SỬA: Map từ trường 'isMain' của Domain Model sang trường 'main' của JPA
    // Entity
    @Mapping(target = "id", source = "id")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "isMain", source = "main") // Đồng bộ trường 'isMain' (Lombok sinh getter là isMain()) sang 'main'
                                                 // của Entity
    @Mapping(target = "product", ignore = true)
    ProductImageEntity toEntityImage(ProductImage domainImage);

    // 3. ĐỒNG BỘ MẢNG ẢNH HAI CHIỀU
    @AfterMapping
    default void synchrousPhysicalImages(Product domain, @MappingTarget ProductEntity entity) {
        if (domain == null || entity == null)
            return;

        List<ProductImageEntity> entityImages = new ArrayList<>();

        // Sử dụng hàm getter an toàn getImages() đã bọc bảo vệ của Product mới
        if (domain.getImages() != null) {
            for (ProductImage domImg : domain.getImages()) {
                if (domImg != null) {
                    ProductImageEntity imgEntity = toEntityImage(domImg);
                    imgEntity.setProduct(entity); // Gán thực thể cha làm khóa ngoại bắt buộc cho JPA
                    entityImages.add(imgEntity);
                }
            }
        }
        entity.setImages(entityImages);
    }

    // 4. CHUYỂN ĐỔI: Domain Model -> DTO
    // ĐÃ TỐI ƯU: Không dùng ignore nữa! Tận dụng trực tiếp 2 hàm getter tính toán
    // của Domain mới
    // MapStruct sẽ tự động gọi product.getAllImageUrls() map vào DTO.imageUrls
    // và product.getMainImageUrl() map vào DTO.mainImageUrl
    @Mapping(target = "imageUrls", source = "allImageUrls")
    @Mapping(target = "mainImageUrl", source = "mainImageUrl")
    ProductDTO toDTO(Product product);
}
