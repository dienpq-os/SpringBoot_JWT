package dienpq.domain.port.repository;

import dienpq.domain.model.Product;
import dienpq.domain.model.PagedResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(String maSP);

    List<Product> findAll();

    boolean existsById(String maSP);

    void deleteById(String maSP);

    // Tìm kiếm và phân trang trả về PagedResult thuần Java
    PagedResult<Product> searchProductsPaginated(String keyword, int page, int size);

    // CÁC HÀM THỐNG KÊ SỬ DỤNG CHO DASHBOARD
    // 1. Tính tổng giá trị tồn kho
    BigDecimal sumTotalInventoryValue();

    // 2. Đếm sản phẩm sắp hết hàng
    long countLowStock(int threshold);

    // 3. Đếm sản phẩm hết hàng (soLuong = 0)
    long countOutOfStock();

    // 4. Lấy danh sách sản phẩm tồn kho thấp
    List<Product> findBySoLuongLessThan(int threshold);

    List<Product> findBySoLuongBetween(Integer start, Integer end);

    // 5. Khai báo cổng lấy dữ liệu thống kê số lượng sản phẩm theo hãng
    Map<String, Long> getCountByBrand();

    // 6. Khai báo cổng đếm tổng số lượng sản phẩm hiện có trong hệ thống
    long countTotal();

}