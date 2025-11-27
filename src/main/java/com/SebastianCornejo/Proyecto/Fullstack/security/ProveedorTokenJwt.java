package com.SebastianCornejo.Proyecto.Fullstack.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class ProveedorTokenJwt {

    private final Key key;
    private final long jwtExpirationMs;

    public ProveedorTokenJwt(
            @Value("${jwt.secret:change-me-please}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long jwtExpirationMs
    ) {
        byte[] keyBytes = deriveKeyBytes(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    private byte[] deriveKeyBytes(String secret) {
        if (secret == null || secret.isBlank()) {
            secret = "change-me-please-change-me-please-change-me-please!";
        }
        if (secret.startsWith("base64:")) {
            String b64 = secret.substring("base64:".length());
            try {
                return Base64.getDecoder().decode(b64);
            } catch (IllegalArgumentException ignored) {
                // fallback to utf8 below
            }
        }
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 32) return raw;
        // pad to at least 32 bytes for HS256
        byte[] padded = new byte[32];
        for (int i = 0; i < padded.length; i++) {
            padded[i] = raw[i % raw.length];
        }
        return padded;
    }

    public String generarToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String obtenerUsernameDesdeToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
