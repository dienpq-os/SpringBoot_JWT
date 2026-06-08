package dienpq.domain.model;

import java.util.List;
import lombok.Value;

@Value // Tạo Class Immutable (bất biến) thuần Java
public class PagedResult<T> {
    List<T> content;
    int currentPage;
    int totalPages;
    long totalElements;
}