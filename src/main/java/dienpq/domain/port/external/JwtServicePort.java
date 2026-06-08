package dienpq.domain.port.external;

public interface JwtServicePort {
    String generateToken(String username);

    String extractUsername(String token);

    boolean validateToken(String token, String username);
}