package dienpq.application.dto;

import dienpq.domain.model.JwtToken;

public class LoginResponse {
    private String token;
    private String type;
    private Long expiresIn;
    private UserDTO user;

    public LoginResponse(JwtToken jwtToken, UserDTO user) {
        this.token = jwtToken.getAccessToken();
        this.type = jwtToken.getTokenType();
        this.expiresIn = jwtToken.getExpiresIn();
        this.user = user;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UserDTO getUser() {
        return user;
    }
}