package com.SebastianCornejo.Proyecto.Fullstack.security;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Usuario;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioUsuario;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioDetallesUsuario implements UserDetailsService {

    private final RepositorioUsuario repositorioUsuario;

    public ServicioDetallesUsuario(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = repositorioUsuario.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        List<SimpleGrantedAuthority> auths;
        if (user instanceof Empleado emp) {
            if (emp.getRol() != null) {
                auths = List.of(new SimpleGrantedAuthority("ROLE_" + emp.getRol().name()));
            } else {
                auths = List.of(new SimpleGrantedAuthority("ROLE_EMPLEADO"));
            }
        } else if (user instanceof Cliente) {
            auths = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        } else {
            auths = List.of();
        }
        return new org.springframework.security.core.userdetails.User(
                user.getCorreo(),
                user.getContrasena(),
                user.getHabilitado(),
                true,
                true,
                true,
                auths
        );
    }
}
