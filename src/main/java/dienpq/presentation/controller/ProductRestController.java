package dienpq.presentation.controller;

import dienpq.application.dto.ProductDTO;
import dienpq.application.service.ProductAppService;
import dienpq.domain.model.DomainFile;
import dienpq.domain.model.Product;
import dienpq.presentation.dto.ProductRequest;
import dienpq.presentation.dto.ProductResponse;
import dienpq.presentation.mapper.ProductWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductRestController {
    private final ProductAppService productService;
    private final ProductWebMapper webMapper;

    // Danh sách các định dạng ảnh an toàn được phép tải lên hệ thống
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final int MAX_FILE_COUNT = 5; // Giới hạn tối đa 5 ảnh cho mỗi sản phẩm

    // 1. API: LƯU SẢN PHẨM MỚI
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('HANHCHINH', 'ADMIN')") // Phòng thủ chiều sâu: Chặn quyền ngay tại Controller
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("product") @Valid ProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> files,
            @RequestParam(value = "mainImageIndex", defaultValue = "0") int mainImageIndex,
            Principal principal) {

        // Đảm bảo bắt buộc phải đăng nhập mới được thực hiện hành động này
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ProductDTO productDTO = webMapper.toDTO(request);
        List<DomainFile> domainFiles = toSecureDomainFiles(files);
        String username = principal.getName();

        // Thực thi nghiệp vụ lõi
        Product savedProduct = productService.save(productDTO, domainFiles, mainImageIndex, username);

        return new ResponseEntity<>(webMapper.toResponse(savedProduct), HttpStatus.CREATED);
    }

    // 2. API: CẬP NHẬT TOÀN DIỆN SẢN PHẨM
    @PutMapping(value = "/{maSP}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('HANHCHINH', 'ADMIN')")
    public ResponseEntity<Void> updateProduct(
            @PathVariable String maSP,
            @RequestPart("product") @Valid ProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> files,
            @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
            @RequestParam(value = "mainImageId", required = false) String mainImageId,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProductDTO appDto = webMapper.toDTO(request);
        List<DomainFile> domainFiles = toSecureDomainFiles(files);
        String username = principal.getName();

        // Thực thi nghiệp vụ lõi
        productService.update(maSP, appDto, domainFiles, deleteImageIds, mainImageId, username);

        return ResponseEntity.noContent().build();
    }

    // 3. API: XÓA SẢN PHẨM
    @DeleteMapping("/{maSP}")
    @PreAuthorize("hasAnyRole('HANHCHINH', 'ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String maSP, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = principal.getName();
        productService.delete(maSP, username);

        return ResponseEntity.noContent().build();
    }

    // Hàm tiện ích bóc tách byte và xử lý làm sạch tệp tin an toàn (Security
    // Hardening)
    private List<DomainFile> toSecureDomainFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        // Chống DoS: Giới hạn số lượng tệp tin gửi lên trong một request
        if (files.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException(
                    "Vượt quá số lượng tệp tin cho phép tối đa (" + MAX_FILE_COUNT + " tệp).");
        }

        List<DomainFile> domainFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {

                // 1. Kiểm tra mã độc Content-Type (MIME type)
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Định dạng nội dung tệp tin không hợp lệ. Chỉ chấp nhận ảnh.");
                }

                // 2. Chống Path Traversal: Làm sạch tên file và bóc tách phần mở rộng
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                if (originalFilename.contains("..")) {
                    throw new IllegalArgumentException("Tên tệp tin không hợp lệ (Phát hiện hành vi Path Traversal).");
                }

                String fileExtension = StringUtils.getFilenameExtension(originalFilename);
                if (fileExtension == null || !ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                    throw new IllegalArgumentException("Phần mở rộng của tệp tin không được hệ thống cho phép.");
                }

                // 3. Thay đổi tên file thành UUID ngẫu nhiên để chống ghi đè tệp hệ thống và
                // che giấu cấu trúc file gốc
                String secureFilename = UUID.randomUUID().toString() + "." + fileExtension.toLowerCase();

                try {
                    domainFiles.add(new DomainFile(
                            "/images/products",
                            secureFilename,
                            file.getSize(),
                            file.getBytes()));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Không thể đọc dữ liệu tệp tin: " + originalFilename, e);
                }
            }
        }
        return domainFiles;
    }
}