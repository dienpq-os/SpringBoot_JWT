package dienpq.infrastructure.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import dienpq.domain.port.external.TransactionManagerPort;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TransactionManagerAdapter implements TransactionManagerPort {

    // Công cụ quản lý Transaction dạng lập trình
    // (Programmatic Transaction) của Spring
    private final TransactionTemplate transactionTemplate;

    @Override
    public <T> T executeInTransaction(Supplier<T> action) {
        // Mở transaction, thực thi hàm callback và
        // tự động commit nếu không có RuntimeException
        return transactionTemplate.execute(status -> action.get());
    }

    @Override
    public void runInTransaction(Runnable action) {
        // Tương tự cho các hàm không cần trả về kết quả
        transactionTemplate.executeWithoutResult(status -> action.run());
    }
}