package br.com.adbrascosmopolis.saac_security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtService {

    private final RsaKeyProvider rsaKeyProvider;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public JwtService(RsaKeyProvider rsaKeyProvider) {
        this.rsaKeyProvider = rsaKeyProvider;
    }

    public String tokenGenerate(String email, Long userId, Long unityId, Collection<String> authorities) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("unityId", unityId)
                .claim("authorities", authorities)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(rsaKeyProvider.getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUnityId(String token) {
        return extractClaim(token, c -> c.get("unityId", Long.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, c -> c.get("userId", Long.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        return extractClaim(token, c -> c.get("authorities", List.class));
    }

    public boolean validToken(String token, String email) {
        String emailDoToken = extractEmail(token);
        return emailDoToken.equals(email) && !expiredToken(token);
    }

    private boolean expiredToken(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(rsaKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
