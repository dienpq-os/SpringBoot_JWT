package dienpq.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private String role;
    private String imageUrl; // Lưu đường dẫn hoặc tên file ảnh đại diện
}