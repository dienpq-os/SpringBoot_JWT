package dienpq.infrastructure.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dienpq.infrastructure.adapter.persistence.entity.ProductImageEntity;

import java.util.List;

@Repository
public interface ProductImageJpaRepository extends JpaRepository<ProductImageEntity, Long> {

    List<ProductImageEntity> findByProductMaSP(String maSP);

    void deleteByProductMaSP(String maSP);

}
