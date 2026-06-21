package dienpq.infrastructure.adapter.external;

import dienpq.domain.model.JwtToken;
import dienpq.domain.model.User;
import dienpq.domain.port.external.TokenServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenServiceAdapter implements TokenServicePort {

    @Value("${app.security.jwt.secret:YOUR_SUPER_SECRET_KEY_CHOOSE_A_LONG_ONE_FOR_HMAC}")
    private String secretKey;

    @Value("${app.security.jwt.expiration-ms:8640000}") // 2,4 giờ
    private long jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = this.secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public JwtToken generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole()); // Role là Enum

        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) // Hoặc user.getUsername() tùy thiết kế
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // Khởi tạo đối tượng JwtToken của tầng Domain
        return new JwtToken(token, "Bearer", expiration.getTime());
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getEmail()) && !isTokenExpired(token));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser() // 1. Dùng parser() thay vì parserBuilder()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))) // 2. Dùng verifyWith() thay
                                                                                            // cho setSigningKey()
                .build() // 3. Khởi tạo parser
                .parseSignedClaims(token) // 4. Dùng parseSignedClaims() thay cho parseClaimsJws()
                .getPayload(); // 5. Dùng getPayload() thay cho getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    @Override
    public String getUsernameFromToken(String token) {
        // Giải mã token bằng cú pháp jjwt mới (parser)
        Claims claims = Jwts.parser() // 1. Dùng parser() thay vì parserBuilder()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))) // 2. Dùng verifyWith() thay
                                                                                            // cho setSigningKey()
                .build() // 3. Khởi tạo parser
                .parseSignedClaims(token) // 4. Dùng parseSignedClaims() thay cho parseClaimsJws()
                .getPayload(); // 5. Dùng getPayload() thay cho getBody();

        // Trích xuất username (thường được lưu trong phần 'subject' của JWT)
        return claims.getSubject();
    }
}