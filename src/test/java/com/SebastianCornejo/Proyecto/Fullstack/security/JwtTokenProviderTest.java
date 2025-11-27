package com.SebastianCornejo.Proyecto.Fullstack.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "01234567890123456789012345678901-01234567890123456789012345678901"; // >= 64 bytes

    @Test
    @DisplayName("generarToken devuelve JWT válido y validarToken es true")
    void testGenerateAndValidateToken() {
        ProveedorTokenJwt provider = new ProveedorTokenJwt(SECRET, 3600_000L);
        UserDetails principal = User.withUsername("user@example.com").password("pw").roles("CLIENT").build();
        String token = provider.generarToken(principal);
        assertNotNull(token);
        assertTrue(provider.validarToken(token));
        assertEquals("user@example.com", provider.obtenerUsernameDesdeToken(token));
    }

    @Test
    @DisplayName("validateToken returns false for tampered token")
    void testValidateTamperedToken() {
        ProveedorTokenJwt provider = new ProveedorTokenJwt(SECRET, 3600_000L);
        UserDetails principal = User.withUsername("user@example.com").password("pw").roles("CLIENT").build();
        String token = provider.generarToken(principal);
        // Tamper the token by appending an extra character
        String tampered = token + "x";
        assertFalse(provider.validarToken(tampered));
    }

    @Test
    @DisplayName("validateToken returns false for expired token")
    void testExpiredToken() throws InterruptedException {
        ProveedorTokenJwt provider = new ProveedorTokenJwt(SECRET, 1L); // 1 ms
        UserDetails principal = User.withUsername("expired@example.com").password("pw").roles("CLIENT").build();
        String token = provider.generarToken(principal);
        Thread.sleep(10L);
        assertFalse(provider.validarToken(token));
    }
}