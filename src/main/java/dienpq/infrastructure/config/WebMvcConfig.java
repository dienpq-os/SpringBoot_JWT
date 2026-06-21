package dienpq.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {

                // 1. Cấu hình tài nguyên tĩnh hệ thống (CSS, JS, Fonts)
                registry.addResourceHandler("/static/**", "/css/**", "/js/**")
                                .addResourceLocations("classpath:/static/", "classpath:/static/css/",
                                                "classpath:/static/js/")
                                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());

                // 2. 🌟 ÁNH XẠ THƯ MỤC ẢNH:
                // Khai báo đồng thời cả thư mục gốc và thư mục con "users/"
                // để Front End nhận dạng được tệp tin
                registry.addResourceHandler("/images/**")
                                .addResourceLocations(
                                                "file:./uploads/images/",
                                                "file:///C:/WORKS/HOC-JAVA/02_SpringProjects/3_Clean_Architecture_WEB/uploads/images/",
                                                "file:///C:/WORKS/HOC-JAVA/02_SpringProjects/3_Clean_Architecture_WEB/uploads/images/users/",
                                                "file:///C:/WORKS/HOC-JAVA/02_SpringProjects/3_Clean_Architecture_WEB/uploads/images/products/")
                                .setCacheControl(
                                                CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate().mustRevalidate());
        }
}