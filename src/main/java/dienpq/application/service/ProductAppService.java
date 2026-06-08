package dienpq.application.service;

import dienpq.application.dto.ProductDTO;
import dienpq.domain.model.DomainFile;
import dienpq.domain.model.PagedResult;
import dienpq.domain.model.Product;
import java.util.List;

public interface ProductAppService {

    // Thực hiện tìm kiếm và phân trang sản phẩm
    public PagedResult<Product> listPagedResult(String keyword, int page, int size);

    // Lấy tổng giá trị tồn kho phục vụ hiển thị nhanh trên trang danh sách
    public double getInventoryValue();

    public Product getProductById(String maSP);

    public void update(String maSP, ProductDTO dto, List<DomainFile> images,
            List<Long> deleteImageIds, String mainImageId, String username);

    public void delete(String maSP, String username);

    public Product save(ProductDTO dto, List<DomainFile> images, int mainImageIndex, String username);

}