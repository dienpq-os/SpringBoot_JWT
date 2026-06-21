package dienpq.presentation.mapper;

import dienpq.application.dto.UserDTO;
import dienpq.application.dto.LoginRequest;
import dienpq.domain.model.User;
import dienpq.presentation.dto.UserRequest;
import dienpq.presentation.dto.UserResponse;
import dienpq.presentation.dto.AuthRequest;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserWebMapper {
    UserDTO toDTO(UserRequest request);

    // Chuyển Request từ Client thành Domain Model sạch; Bỏ qua password vì Request
    // không chứa password thô để map trực tiếp (phải qua encode ở Service)
    @Mapping(target = "password", ignore = true)
    User toDomain(UserRequest request);

    // Chuyển đổi từ Domain Model ra Response sạch
    // (Tự động bỏ qua trường password)
    UserResponse toResponse(User user);

    // Chuyển từ Lõi Domain ngược ra Request
    // Phục vụ hiển thị form cập nhật (Edit Form)
    UserRequest toRequest(User user);

    // Tự động lặp qua danh sách đối tượng User sang danh sách UserResponse
    List<UserResponse> toResponseList(List<User> users);

    // Ánh xạ từ trường 'email' của AuthRequest sang trường 'username' của
    // LoginRequest
    @Mapping(source = "email", target = "username")
    LoginRequest toLoginRequest(AuthRequest authRequest);
}