package dienpq.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private Integer id;
    private String username;
    private String email;
    private String role;
    private String imageUrl;
}