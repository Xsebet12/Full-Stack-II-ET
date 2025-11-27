package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rut"}),
        @UniqueConstraint(columnNames = {"correo_electronico"})
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private String rut; // sin DV

    @Column(nullable = false, length = 1)
    private String dv; // dígito verificador

    @Column(name = "correo_electronico", nullable = false)
    private String correo;


    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean habilitado = true;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private Instant creadoEn = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comuna", nullable = false)
    private Comuna comuna;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    @Builder.Default
    private Role rol = Role.CLIENT;

}
