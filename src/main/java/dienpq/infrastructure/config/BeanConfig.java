package dienpq.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import dienpq.application.service.MyBean;
import dienpq.domain.port.external.JsonSerializerPort;
import dienpq.infrastructure.adapter.external.JacksonSerializerAdapter;

@Configuration
// Tự động quét toàn bộ thư mục gốc 'dienpq'
// Tìm kiếm tất cả các lớp được đánh dấu bằng @MyBean
// để tự động đăng ký làm Spring Bean
// Giữ lại cơ chế quét các @Component, @Repository mặc định của tầng hạ tầng
@ComponentScan(basePackages = "dienpq.application.service", includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = MyBean.class), useDefaultFilters = true)
public class BeanConfig {

    // CHỈ GIỮ LẠI:
    // Các Bean hạ tầng đặc thù
    // cần bọc thư viện bên ngoài (như Jackson ObjectMapper)
    @Bean
    public JsonSerializerPort jsonSerializerPort(ObjectMapper objectMapper) {
        return new JacksonSerializerAdapter(objectMapper);
    }

    // Khởi tạo và nạp Bean AuthenticationManager vào Context của Spring Application
    // Phục vụ trực tiếp cho việc xác thực tài khoản thủ công tại ApiAuthController
    // (Luồng JWT)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}