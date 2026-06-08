package dienpq.presentation.mapper;

import dienpq.application.dto.ProductDTO;
import dienpq.domain.model.Product;
import dienpq.domain.model.ProductImage;
import dienpq.presentation.dto.ProductRequest;
import dienpq.presentation.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductWebMapper {

    // 1. Chuyển Request từ Client thành Domain Model sạch
    @Mapping(target = "images", ignore = true)
    Product toDomain(ProductRequest request);

    // 2. Chuyển Domain Model thành Response trả về Client
    @Mapping(target = "mainImageUrl", source = "mainImageUrl")
    ProductResponse toResponse(Product product);

    // 3. Tự động sinh code chạy vòng lặp cho danh sách
    List<ProductResponse> toResponseList(List<Product> products);

    // 4. Các hàm ánh xạ DTO tầng ứng dụng
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "mainImageUrl", ignore = true)
    ProductDTO toDTO(ProductRequest request);

    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "mainImageUrl", ignore = true)
    ProductDTO toDTO(Product product);

    // 5. Chuyển từ Lõi Domain ngược ra Form Giao diện
    ProductRequest toRequest(Product product);

    // 6. Chỉ định rõ ràng thuộc tính mục tiêu là "isMain"
    // (khớp với tên biến DTO)
    // và lấy nguồn từ phương thức "isMain" của thực thể Domain ProductImage
    @Mapping(target = "isMain", source = "main")
    ProductResponse.ProductImageResponse toImageResponse(ProductImage productImage);
}