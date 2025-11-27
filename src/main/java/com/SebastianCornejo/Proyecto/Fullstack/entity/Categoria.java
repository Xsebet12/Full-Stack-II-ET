package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    @JsonProperty("nombre")
    private String nombre;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"categoria"})
    @JsonProperty("productos")
    private Set<Producto> productos;
}
