package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "providers")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String serviceType;

    private String url;

    private String phone;

    private String logoUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProveedor estado = EstadoProveedor.ACTIVO;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"proveedor"})
    private Set<ContactoProveedor> contacts;
}
