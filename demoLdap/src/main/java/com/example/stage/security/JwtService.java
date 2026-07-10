package com.example.stage.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey key = Jwts.SIG.HS256.key().build();
    private final long expirationMs = 3_600_000; // 1 hour

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities, String structure) {
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("structure",structure)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
    public String extractStructure(String token){
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("structure",String.class);
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        String roles = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("roles", String.class);

        if (roles == null || roles.isBlank()) return List.of();

        return Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}