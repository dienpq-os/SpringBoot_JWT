package dienpq.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {

                // 1. Cấu hình tài nguyên tĩnh hệ thống (CSS, JS, Fonts)
                // Cho phép trình duyệt cache 365 ngày để tối ưu hiệu năng vì các file này cố
                // định và an toàn
                registry.addResourceHandler("/static/**", "/css/**", "/js/**")
                                .addResourceLocations("classpath:/static/", "classpath:/static/css/",
                                                "classpath:/static/js/")
                                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());

                // 2. TỐI ƯU BẢO MẬT: Cấu hình thư mục tải lên của người dùng (Uploads)
                // Ép buộc trình duyệt KHÔNG ĐƯỢC PHÉP tự ý thực thi hoặc đoán định dạng file
                // sai mục đích (Chống XSS/RCE)
                // Chỉ cho phép lưu cache ngắn hạn (hoặc không lưu) đối với dữ liệu người dùng
                // nhằm tránh rò rỉ trên máy tính công cộng
                registry.addResourceHandler("/images/**")
                                .addResourceLocations("file:uploads/images/")
                                .setCacheControl(
                                                CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate().mustRevalidate());
        }

        /**
         * BỔ SUNG: Cấu hình chính sách chia sẻ tài nguyên cấu hình nghiêm ngặt (CORS
         * Hardening)
         * Ngăn chặn các website lạ gọi Javascript tới API hệ thống của bạn để đánh cắp
         * dữ liệu
         */
        @Override
        public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**") // Chỉ áp dụng cho các cổng API
                                // Thay vì dùng "*" nguy hiểm, hãy điền chính xác domain Frontend của bạn (ví
                                // dụ: http://localhost:3000 hoặc domain công ty)
                                .allowedOrigins("http://localhost:8080", "https://yourdomain.com")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("Content-Type", "X-XSRF-TOKEN", "Authorization") // Chỉ cho phép các
                                                                                                 // Header an toàn
                                .allowCredentials(true) // Bắt buộc bằng true nếu hệ thống sử dụng cơ chế Session-Cookie
                                                        // / CSRF Cookie
                                .maxAge(3600); // Tối ưu hóa số lần gửi request pre-flight trong 1 tiếng
        }
}