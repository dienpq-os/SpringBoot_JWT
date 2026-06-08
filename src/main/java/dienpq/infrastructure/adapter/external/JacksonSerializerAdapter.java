package dienpq.infrastructure.adapter.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import dienpq.domain.port.external.JsonSerializerPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JacksonSerializerAdapter implements JsonSerializerPort {
    private final ObjectMapper objectMapper;

    @Override
    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            // Ném ra RuntimeException để hệ thống phía trên xử lý hoặc ghi log
            throw new RuntimeException("Lỗi chuyển đổi dữ liệu sang JSON", e);
        }
    }
}