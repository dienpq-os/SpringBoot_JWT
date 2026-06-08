package dienpq.domain.port.external;

import java.util.function.Supplier;

public interface TransactionManagerPort {
    <T> T executeInTransaction(Supplier<T> action); // Dùng cho hàm có giá trị trả về (như Create, Update)

    void runInTransaction(Runnable action); // Dùng cho hàm void (như Delete)
}