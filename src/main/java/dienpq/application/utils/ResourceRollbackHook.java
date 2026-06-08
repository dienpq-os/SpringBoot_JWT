package dienpq.application.utils;

import java.util.function.Consumer;

//Điều khiển tiến trình dọn dẹp tài nguyên khi Use Case bị thất bại.
//Nằm hoàn toàn tại tầng Application, thuần Java 100%.
public class ResourceRollbackHook implements AutoCloseable {

    private final String[] targetUrls;
    private final Consumer<String> rollbackAction; // Nhận con trỏ hàm từ Domain Port
    private boolean isSuccess = false;

    public ResourceRollbackHook(Consumer<String> rollbackAction, String... targetUrls) {
        this.rollbackAction = rollbackAction;
        this.targetUrls = targetUrls;
    }

    public void commit() {
        this.isSuccess = true;
    }

    @Override
    public void close() {
        if (isSuccess || targetUrls == null || targetUrls.length == 0) {
            return;
        }

        for (String url : targetUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }

            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            try {
                // Tầng Application gọi hành động thông qua Port được truyền vào
                rollbackAction.accept(url);
            } catch (Throwable t) {
                // Khôi phục trạng thái ngắt luồng hệ thống nếu gặp lỗi chí mạng
                if (t instanceof InterruptedException || t.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}