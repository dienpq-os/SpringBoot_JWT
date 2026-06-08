package dienpq.infrastructure.adapter.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dienpq.infrastructure.adapter.persistence.entity.ProductEntity;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {

        // 1. Sử dụng Query tường minh và
        // bọc countQuery để phân trang 100% tại DB (giảm tải RAM)
        @EntityGraph(attributePaths = { "images" })
        @Query(value = "SELECT p FROM ProductEntity p WHERE " +
                        "LOWER(p.maSP) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.tenModel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.hangSanXuat) LIKE LOWER(CONCAT('%', :keyword, '%'))", countQuery = "SELECT COUNT(p) FROM ProductEntity p WHERE "
                                        +
                                        "LOWER(p.maSP) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                                        "LOWER(p.tenModel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                                        "LOWER(p.hangSanXuat) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<ProductEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        // --- Tối ưu thống kê Dashboard ---

        @Query("SELECT SUM(p.giaBan * p.soLuong) FROM ProductEntity p")
        Double sumTotalInventoryValue();

        // 2. Dùng Interface Projection để đếm số lượng sản phẩm theo hãng,
        // tránh phải load toàn bộ entity vào RAM
        @Query("SELECT p.hangSanXuat AS brand, COUNT(p) AS count FROM ProductEntity p GROUP BY p.hangSanXuat")
        List<BrandCountProjection> countProductsByBrand();

        // Đếm số lượng sản phẩm có số lượng thấp (ví dụ: soLuong < 5)
        @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.soLuong < :threshold")
        long countLowStock(@Param("threshold") int threshold);

        // Đếm số lượng sản phẩm hết hàng (soLuong = 0)
        @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.soLuong = 0")
        long countOutOfStock();

        // 3. Dùng EntityGraph để tối ưu truy vấn lấy sản phẩm có số lượng thấp,
        // tránh N+1 khi load ảnh (images) liên quan
        @EntityGraph(attributePaths = { "images" })
        List<ProductEntity> findBySoLuongLessThan(int threshold);

        // Định nghĩa cấu trúc DTO ảo phục vụ cho hàm thống kê hãng
        interface BrandCountProjection {
                String getBrand();

                Long getCount();
        }
}