package com.SebastianCornejo.Proyecto.Fullstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.Objects;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Region;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Role;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Usuario;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Categoria;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Producto;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioCategoria;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProducto;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Comuna;

@SpringBootApplication
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class ProyectoFullstackLlReactApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProyectoFullstackLlReactApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner datosIniciales(RepositorioUsuario repositorioUsuario,
                                            PasswordEncoder codificadorContrasena,
                                            RepositorioCategoria repositorioCategoria,
                                            RepositorioProducto repositorioProducto,
                                            com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioRegion repoRegion,
                                            com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioComuna repoComuna,
                                            PlatformTransactionManager transactionManager) {
        TransactionTemplate tx = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        return args -> tx.execute(status -> {
            // Semilla de usuario ADMIN idempotente (evita duplicados por correo y rut)
            String[] correosCandidatos = {"admin@example.com", "admin1@example.com", "admin2@example.com"};
            String[] rutsCandidatos = {"11111111", "11111112", "22222222"};
            String correoAdmin = java.util.Arrays.stream(correosCandidatos)
                    .filter(e -> !repositorioUsuario.existsByCorreo(e))
                    .findFirst()
                    .orElse(null);
            String rutAdmin = java.util.Arrays.stream(rutsCandidatos)
                    .filter(r -> !repositorioUsuario.existsByRut(r))
                    .findFirst()
                    .orElse(null);
            if (correoAdmin != null && rutAdmin != null) {
            // Asegurar que exista una región y comuna por defecto y asignarlas al admin
            Comuna comunaPorDefecto = null;
            if (repoRegion != null && repoComuna != null) {
                Region region = repoRegion.findByNomRegionIgnoreCase("Metropolitana")
                                .orElseGet(() -> repoRegion.save(Region.builder().idRegion(13).nomRegion("Metropolitana").build()));
                comunaPorDefecto = repoComuna.findByNomComunaIgnoreCase("Santiago")
                                .orElseGet(() -> repoComuna.save(Comuna.builder().idComuna(13101).nomComuna("Santiago").region(region).build()));
            }
            com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado administrador = new com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado();
            administrador.setNombres("Admin");
            administrador.setApellidos("User");
            administrador.setRut(rutAdmin);
            administrador.setDv("1");
            administrador.setCorreo(correoAdmin);
            administrador.setContrasena(codificadorContrasena.encode("admin123"));
            administrador.setDireccion("Santiago");
            administrador.setRol(Role.ADMIN);
            if (comunaPorDefecto != null) administrador.setComuna(comunaPorDefecto);
            repositorioUsuario.save(Objects.requireNonNull(administrador));
                System.out.println("Usuario ADMIN sembrado: " + correoAdmin + " / admin123 (rut " + rutAdmin + ")");
            } else {
                System.out.println("Omitiendo semilla ADMIN (correo o rut ya en uso).");
            }

            // Semilla de categoría por defecto "General"
        Categoria general = repositorioCategoria.findByNombreIgnoreCase("General")
            .orElseGet(() -> repositorioCategoria.save(Objects.requireNonNull(Categoria.builder().nombre("General").build())));

            // Semilla de producto demo si no existe ninguno
            if (repositorioProducto.count() == 0) {
        Producto productoDemo = Producto.builder()
            .nombre("Producto Demo")
            .descripcion("Producto de ejemplo")
            .precio(new java.math.BigDecimal("9.99"))
            .stock(100)
            .categoria(general)
                .habilitado(true)
            .build();
        repositorioProducto.save(Objects.requireNonNull(productoDemo));
            }
            return null;
        });
    }
}
