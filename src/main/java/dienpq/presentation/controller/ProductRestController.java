package dienpq.presentation.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import dienpq.application.dto.ProductDTO;
import dienpq.application.service.ProductAppService;
import dienpq.domain.model.DomainFile;
import dienpq.domain.model.PagedResult;
import dienpq.domain.model.Product;
import dienpq.presentation.mapper.ProductWebMapper;
import dienpq.presentation.dto.ProductRequest;
import dienpq.presentation.dto.ProductResponse;
import dienpq.presentation.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductRestController {

    private final ProductWebMapper productMapper;
    private final ProductAppService productService;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final int MAX_FILE_COUNT = 5;

    // API lấy tổng giá trị tồn kho riêng (Có Cache)
    @GetMapping("/inventory-value")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "inventory", key = "'total_value'") // Đặt tên vùng cache và key tường minh
    public ResponseEntity<ApiResponse<BigDecimal>> getInventoryValue() {

        BigDecimal totalValue = productService.getInventoryValue();

        // Trả về cấu trúc bọc ApiResponse nhất quán với toàn hệ thống
        return ResponseEntity.ok(
                ApiResponse.success("Lấy tổng giá trị tồn kho thành công!", totalValue));
    }

    // API 1: LẤY DANH SÁCH SẢN PHẨM PHÂN TRANG
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResult<ProductResponse>>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Tham số phân trang (page, size) không hợp lệ.");
        }
        // 1. Gọi Service lấy dữ liệu gốc (Entity)
        PagedResult<Product> productPage = productService.listPagedResult(keyword, page, size);

        // 2. Chuyển đổi List<Product> thành List<ProductResponse> qua MapStruct
        List<ProductResponse> responseContent = productMapper.toResponseList(productPage.getContent());

        // 3. Tái sử dụng cấu trúc PagedResult để đóng gói dữ liệu phản hồi
        PagedResult<ProductResponse> pagedResponse = new PagedResult<>(
                responseContent,
                productPage.getCurrentPage(),
                productPage.getTotalPages(),
                productPage.getTotalElements());

        // 4. Trả về cấu trúc ApiResponse đồng bộ
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách sản phẩm thành công!", pagedResponse));
    }

    // API 2: LẤY CHI TIẾT SẢN PHẨM THEO MÃ SP
    @GetMapping("/{maSP}")
    @PreAuthorize("hasAnyAuthority('ROLE_HANHCHINH', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetail(@PathVariable String maSP) {

        // Tầng Service chịu trách nhiệm tìm kiếm và ném lỗi nếu không thấy
        // (Ví dụ: ResourceNotFoundException)
        Product product = productService.getProductById(maSP);

        // Sử dụng Mapper để chuyển đổi sang cấu trúc ProductResponse chuẩn chỉnh
        ProductResponse productResponse = productMapper.toResponse(product);

        // Trả về cấu trúc bọc ApiResponse nhất quán
        return ResponseEntity.ok(
                ApiResponse.success("Lấy chi tiết sản phẩm thành công!", productResponse));
    }

    // API 3: LƯU SẢN PHẨM MỚI (Multipart Form Data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_HANHCHINH', 'ROLE_ADMIN')")
    @CacheEvict(value = "inventory", key = "'total_value'") // XÓA CACHE CŨ KHI CÓ SẢN PHẨM MỚI THÊM VÀO
    public ResponseEntity<ApiResponse<Void>> saveProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "mainImageIndex", defaultValue = "0") int mainImageIndex,
            Authentication authentication) {

        int imageCount = (images != null) ? images.length : 0;
        if (mainImageIndex < 0 || (imageCount > 0 && mainImageIndex >= imageCount)) {
            throw new IllegalArgumentException("Chỉ số ảnh chính không hợp lệ.");
        }

        List<DomainFile> domainFiles = toSecureDomainFiles(images);
        ProductDTO productDTO = productMapper.toDTO(request);
        productService.save(productDTO, domainFiles, mainImageIndex, authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("✅ Thêm sản phẩm kèm album ảnh thành công!"));
    }

    // API 4: CẬP NHẬT SẢN PHẨM
    @PutMapping(value = "/{maSP}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_HANHCHINH', 'ROLE_ADMIN')")
    @CacheEvict(value = "inventory", key = "'total_value'") // Xóa bộ nhớ đệm giá trị tồn kho cũ
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable String maSP,
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
            @RequestParam(value = "mainImageId", required = false) String mainImageId,
            Authentication authentication) {

        // Xử lý nghiệp vụ sạch sẽ, không boilerplate try-catch
        List<DomainFile> domainFiles = toSecureDomainFiles(images);
        ProductDTO appDto = productMapper.toDTO(request);

        productService.update(maSP, appDto, domainFiles, deleteImageIds, mainImageId, authentication.getName());

        // Trả về Response Object chuẩn hóa của hệ thống
        return ResponseEntity.ok(
                ApiResponse.success("✅ Cập nhật sản phẩm và album ảnh thành công!"));
    }

    // API 5: XÓA SẢN PHẨM
    @DeleteMapping("/{maSP}")
    @PreAuthorize("hasAnyAuthority('ROLE_HANHCHINH', 'ROLE_ADMIN')")
    @CacheEvict(value = "inventory", key = "'total_value'") // Xóa bộ nhớ đệm giá trị tồn kho
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable("maSP") String maSP,
            Authentication authentication) {

        // Logic nghiệp vụ sạch sẽ, giao việc xử lý ngoại lệ cho Global Exception
        productService.delete(maSP, authentication.getName());

        // Trả về Response Object đồng bộ
        return ResponseEntity.ok(
                ApiResponse.success("✅ Đã xóa sản phẩm thành công!"));
    }

    // Tiện ích bóc tách xử lý file ảnh an toàn
    private List<DomainFile> toSecureDomainFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return List.of();
        }
        if (files.length > MAX_FILE_COUNT) {
            throw new IllegalArgumentException(
                    "Vượt quá số lượng tệp ảnh cho phép tối đa (" + MAX_FILE_COUNT + " tệp).");
        }

        List<DomainFile> domainFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Chỉ chấp nhận các tệp tin định dạng ảnh hợp lệ.");
                }

                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                if (originalFilename.contains("..")) {
                    throw new IllegalArgumentException("Phát hiện hành vi tấn công thao túng đường dẫn tệp tin.");
                }

                String fileExtension = StringUtils.getFilenameExtension(originalFilename);
                if (fileExtension == null || !ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                    throw new IllegalArgumentException("Hệ thống không hỗ trợ phần mở rộng tệp này.");
                }

                String secureFilename = UUID.randomUUID().toString() + "." + fileExtension.toLowerCase();
                try {
                    domainFiles.add(new DomainFile(
                            "/images/products",
                            secureFilename,
                            file.getSize(),
                            file.getBytes()));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Không thể đọc dữ liệu tệp tải lên.", e);
                }
            }
        }
        return domainFiles;
    }
}