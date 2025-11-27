package com.SebastianCornejo.Proyecto.Fullstack.testconfig;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import com.SebastianCornejo.Proyecto.Fullstack.security.FiltroAutenticacionJwt;
import com.SebastianCornejo.Proyecto.Fullstack.security.ProveedorTokenJwt;
import com.SebastianCornejo.Proyecto.Fullstack.security.ServicioDetallesUsuario;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.access.AccessDeniedException;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    public FiltroAutenticacionJwt jwtAuthenticationFilter() {
        return Mockito.mock(FiltroAutenticacionJwt.class);
    }

    @Bean
    public ProveedorTokenJwt proveedorTokenJwt() {
        return Mockito.mock(ProveedorTokenJwt.class);
    }

    @Bean
    public ServicioDetallesUsuario servicioDetallesUsuario() {
        return Mockito.mock(ServicioDetallesUsuario.class);
    }

    @ControllerAdvice
    @ResponseBody
    static class TestExceptionTranslator {
        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<String> handleAuthorizationDenied(AuthorizationDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
    }
}
