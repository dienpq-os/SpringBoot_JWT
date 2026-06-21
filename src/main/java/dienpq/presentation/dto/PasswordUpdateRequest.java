package dienpq.presentation.dto;

import lombok.Getter;

@Getter
public class PasswordUpdateRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}