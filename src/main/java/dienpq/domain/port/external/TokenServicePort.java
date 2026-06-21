package dienpq.domain.port.external;

import dienpq.domain.model.User;
import dienpq.domain.model.JwtToken;

public interface TokenServicePort {
    JwtToken generateToken(User user);

    String getUsernameFromToken(String token);

    boolean validateToken(String token, User user);

    public String extractUsername(String token);
}