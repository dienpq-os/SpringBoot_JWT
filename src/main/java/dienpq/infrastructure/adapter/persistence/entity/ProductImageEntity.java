package dienpq.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_image", indexes = {
        @Index(name = "idx_product_image_masp", columnList = "product_masp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "product") // QUAN TRỌNG: Loại bỏ products khỏi toString để tránh lỗi StackOverflow
@EqualsAndHashCode(exclude = "product") // QUAN TRỌNG: Tránh lỗi vòng lặp khi so sánh đối tượng
public class ProductImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_masp", referencedColumnName = "maSP")
    private ProductEntity product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private boolean isMain = false;

    // Constructor tùy chỉnh cho việc tạo nhanh (giống bản gốc của bạn)
    public ProductImageEntity(String imageUrl, boolean isMain) {
        this.imageUrl = imageUrl;
        this.isMain = isMain;
    }
}