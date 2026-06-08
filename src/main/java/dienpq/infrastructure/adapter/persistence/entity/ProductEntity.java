package dienpq.infrastructure.adapter.persistence.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_ten_model", columnList = "tenModel"),
        @Index(name = "idx_product_hang_san_xuat", columnList = "hangSanXuat")
})
@Getter // Tự động tạo tất cả Getter
@Setter // Tự động tạo tất cả Setter
@NoArgsConstructor // Tự động tạo Constructor rỗng (bắt buộc cho JPA)
@AllArgsConstructor // Tự động tạo Constructor đầy đủ tham số
@Builder // Hỗ trợ tạo đối tượng theo kiểu chuỗi (Fluent API)
public class ProductEntity {

    @Id
    @Column(length = 20)
    private String maSP;

    @Column(length = 100, nullable = false)
    private String tenModel;

    @Column(length = 50, nullable = false)
    private String hangSanXuat;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal giaBan;
    @Column(nullable = false)
    private int soLuong;

    @BatchSize(size = 10) // Tối ưu: Lấy ảnh theo lô để tránh load chậm (N+1 Query)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // Tối ưu: Tránh vòng lặp vô hạn khi in Log (nếu dùng @Data)
    @Builder.Default // Đảm bảo khởi tạo mặc định để tránh NullPointerException khi thêm ảnh mới
    private List<ProductImageEntity> images = new ArrayList<>();

}