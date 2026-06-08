package dienpq.infrastructure.adapter.external;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import dienpq.domain.port.external.UserLoggerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LogbackUserLoggerAdapter implements UserLoggerPort {

    // Trỏ chính xác vào tên Logger riêng biệt đã định nghĩa trong file XML của bạn
    private static final Logger businessLogger = LoggerFactory.getLogger("UserBusinessLogger");

    // Quản lý danh sách các file log cá nhân đang mở
    private final ConcurrentHashMap<String, LoggerContainer> userLoggers = new ConcurrentHashMap<>();

    // Tự động giải phóng file log cá nhân trong RAM nếu User không thao tác sau 30
    // phút
    private static final long IDLE_TIMEOUT_MS = 30 * 60 * 1000;

    @Override
    public void saveLog(String userId, String message) {
        String logUser = (userId == null || userId.trim().isEmpty() || "unknown".equalsIgnoreCase(userId))
                ? "system"
                : userId;

        // 1. Ghi vào file log tổng nghiệp vụ (all_users_activity.log) thông qua XML
        // công nghệ
        try {
            MDC.put("userId", logUser);
            businessLogger.info(message);
        } finally {
            MDC.remove("userId");
        }

        // Nếu là tác vụ hệ thống thông thường, dừng lại (không tạo file riêng lẻ
        // user_system.log)
        if ("system".equals(logUser)) {
            return;
        }

        // Dọn dẹp các luồng log cũ đã treo lâu trong bộ nhớ để bảo vệ RAM
        evictIdleLoggers();

        // 2. Ghi vào file log cá nhân riêng biệt (user_[userId].log) sinh bằng Java thủ
        // công
        LoggerContainer container = userLoggers.computeIfAbsent(logUser, this::createLoggerForUser);
        container.updateLastAccessed();
        container.getLogger().info(message);
    }

    private LoggerContainer createLoggerForUser(String userId) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logbackLogger = context.getLogger("USER_LOG_" + userId);
        logbackLogger.setAdditive(false); // Ngăn không cho bắn ngược log cá nhân lên Console hệ thống

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("FILE-" + userId);
        appender.setFile("./logs/users/user_" + userId + ".log");

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern("./logs/users/user_" + userId + ".%d{yyyy-MM-dd}.%i.log");
        policy.setMaxFileSize(FileSize.valueOf("5MB"));
        policy.setMaxHistory(7);
        policy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level - %msg%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        logbackLogger.addAppender(appender);
        return new LoggerContainer(logbackLogger, appender);
    }

    private void evictIdleLoggers() {
        long now = System.currentTimeMillis();
        userLoggers.forEach((userId, container) -> {
            if (now - container.getLastAccessed() > IDLE_TIMEOUT_MS) {
                userLoggers.remove(userId);
                container.getAppender().stop(); // Đóng kết nối stream file vật lý để giải phóng tài nguyên
                businessLogger.debug("Đã đóng và giải phóng bộ nhớ file log cá nhân của user: {}", userId);
            }
        });
    }

    // Class bọc đóng vai trò lưu trữ thông tin vòng đời của Logger cá nhân
    private static class LoggerContainer {
        private final Logger logger;
        private final RollingFileAppender<ILoggingEvent> appender;
        private long lastAccessed;

        public LoggerContainer(Logger logger, RollingFileAppender<ILoggingEvent> appender) {
            this.logger = logger;
            this.appender = appender;
            this.lastAccessed = System.currentTimeMillis();
        }

        public Logger getLogger() {
            return logger;
        }

        public RollingFileAppender<ILoggingEvent> getAppender() {
            return appender;
        }

        public long getLastAccessed() {
            return lastAccessed;
        }

        public void updateLastAccessed() {
            this.lastAccessed = System.currentTimeMillis();
        }
    }
}