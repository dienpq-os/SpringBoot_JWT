package dienpq.infrastructure.adapter.persistence;

import dienpq.domain.model.PagedResult;
import dienpq.domain.model.Product;
import dienpq.domain.port.repository.ProductRepositoryPort;
import dienpq.infrastructure.adapter.persistence.entity.ProductEntity;
import dienpq.infrastructure.adapter.persistence.mapper.ProductMapper;
import dienpq.infrastructure.adapter.persistence.repository.ProductJpaRepository;
import dienpq.infrastructure.adapter.persistence.repository.ProductJpaRepository.BrandCountProjection; // ĐÃ THÊM: Import Projection
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaProductsRepository;
    private final ProductMapper productMapper;

    @Override
    public List<Product> findAll() {
        return jpaProductsRepository.findAll().stream().map(productMapper::toDomain).toList();
    }

    @Override
    public Optional<Product> findById(String maSP) {
        return jpaProductsRepository.findById(maSP)
                .map(productMapper::toDomain);
    }

    @Override
    public boolean existsById(String maSP) {
        return jpaProductsRepository.existsById(maSP);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Product save(Product product) {
        ProductEntity entity = productMapper.toEntity(product);
        ProductEntity savedEntity = jpaProductsRepository.save(entity);
        return productMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(String maSP) {
        jpaProductsRepository.deleteById(maSP);
    }

    @Override
    public PagedResult<Product> searchProductsPaginated(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("maSP").ascending());

        Page<ProductEntity> entitiesPage;
        // ĐÃ SỬA: Thay đổi phương thức gọi từ hàm sinh tự động (Query Method) sang hàm
        // tự viết (searchByKeyword) để kích hoạt countQuery
        if (keyword == null || keyword.trim().isEmpty()) {
            entitiesPage = jpaProductsRepository.findAll(pageable);
        } else {
            // Hàm này bây giờ sẽ chạy phân trang chuẩn 100% dưới DB, không lo sập RAM
            entitiesPage = jpaProductsRepository.searchByKeyword(keyword.trim(), pageable);
        }

        List<Product> domainProducts = entitiesPage.getContent().stream()
                .map(productMapper::toDomain)
                .toList();

        return new PagedResult<>(
                domainProducts,
                entitiesPage.getNumber(),
                entitiesPage.getTotalPages(),
                entitiesPage.getTotalElements());
    }

    @Override
    public Double sumTotalInventoryValue() {
        Double totalValue = jpaProductsRepository.sumTotalInventoryValue();
        return totalValue != null ? totalValue : 0.0;
    }

    @Override
    public long countTotal() {
        return jpaProductsRepository.count();
    }

    @Override
    public long countLowStock(int threshold) {
        return jpaProductsRepository.countLowStock(threshold);
    }

    @Override
    public long countOutOfStock() {
        return jpaProductsRepository.countOutOfStock();
    }

    @Override
    public List<Product> findBySoLuongLessThan(int threshold) {
        List<ProductEntity> entities = jpaProductsRepository.findBySoLuongLessThan(threshold);
        return entities.stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public Map<String, Long> getCountByBrand() {
        // ĐÃ SỬA: Nhận về danh sách kiểu BrandCountProjection (Interface) thay vì
        // Object[] thô sơ
        List<BrandCountProjection> results = jpaProductsRepository.countProductsByBrand();

        // ĐÃ TỐI ƯU: Không còn dùng mảng result[0], result[1] và không cần ép kiểu thủ
        // công bằng từ khóa instanceof nữa
        return results.stream()
                .filter(p -> p.getBrand() != null && !p.getBrand().isBlank())
                .collect(Collectors.toMap(
                        BrandCountProjection::getBrand, // Gọi trực tiếp hàm lấy tên hãng (Kiểu String chuẩn)
                        BrandCountProjection::getCount, // Gọi trực tiếp hàm lấy số lượng (Kiểu Long chuẩn)
                        (existing, replacement) -> existing));
    }
}