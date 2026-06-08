package dienpq.presentation.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String role;
}
