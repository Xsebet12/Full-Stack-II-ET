package com.SebastianCornejo.Proyecto.Fullstack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    private final ProveedorTokenJwt proveedor;
    private final ServicioDetallesUsuario servicioDetallesUsuario;
    private static final Logger logger = LoggerFactory.getLogger(FiltroAutenticacionJwt.class);

    public FiltroAutenticacionJwt(ProveedorTokenJwt proveedor, ServicioDetallesUsuario servicioDetallesUsuario) {
        this.proveedor = proveedor;
        this.servicioDetallesUsuario = servicioDetallesUsuario;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        logger.debug("Solicitud entrante: {} {} - header Authorization presente: {}", request.getMethod(), request.getRequestURI(), header != null);
        if (header != null && header.startsWith("Bearer ")) {
            logger.debug("Token Bearer detectado para {} {}", request.getMethod(), request.getRequestURI());
            String token = header.substring(7);
            if (proveedor.validarToken(token)) {
                String username = proveedor.obtenerUsernameDesdeToken(token);
                UserDetails userDetails = servicioDetallesUsuario.loadUserByUsername(username);
                logger.debug("Token válido. Usuario autenticado: {} con roles: {}", username, userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Autenticación establecida en SecurityContext para usuario: {} en {} {}", username, request.getMethod(), request.getRequestURI());
            } else {
                logger.debug("Token JWT inválido recibido en {} {}", request.getMethod(), request.getRequestURI());
            }
        } else {
            logger.debug("No se encontró token Bearer para {} {}", request.getMethod(), request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}
