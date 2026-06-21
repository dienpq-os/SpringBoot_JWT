package dienpq.domain.model;

public class JwtToken {
    private final String token;
    private final String tokenType;
    private final long expiresIn; // Thời gian hết hạn tính bằng giây

    public JwtToken(String accessToken, String tokenType, long expiresIn) {
        this.token = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    // Getters
    public String getAccessToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}