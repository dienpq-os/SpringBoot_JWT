package dienpq.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequest {
    @JsonProperty("email") // Đón trường 'email' từ HTML gửi lên
    private String email;
    private String password;

    // Getters và Setters thông thường
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}