package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nombre", nullable = false)
    @JsonProperty("nombre")
    private String nombre;

    @Column(name = "descripcion")
    @JsonProperty("descripcion")
    private String descripcion;

    @Column(name = "precio", precision = 19, scale = 2)
    @JsonProperty("precio")
    private BigDecimal precio;

    @Column(name = "precio_detalle", precision = 19, scale = 2)
    @JsonProperty("precioDetalle")
    private BigDecimal precioDetalle;

    @Column(name = "precio_vip", precision = 19, scale = 2)
    @JsonProperty("precioVip")
    private BigDecimal precioVip;

    @Column(name = "precio_mayorista", precision = 19, scale = 2)
    @JsonProperty("precioMayorista")
    private BigDecimal precioMayorista;

    @Column(name = "costo_referencia", precision = 19, scale = 2)
    @JsonProperty("costoReferencia")
    private BigDecimal costoReferencia;

    @Column(name = "stock")
    @JsonProperty("stock")
    private Integer stock;

    @Column(name = "imagen")
    @JsonProperty("imagen")
    private String imagen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"productos"})
    @JsonProperty("categoria")
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"producto"})
    @JsonProperty("imagenes")
    private Set<ImagenProducto> imagenes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id")
    @JsonIgnoreProperties({"contacts"})
    @JsonProperty("proveedor")
    private Proveedor proveedor;

    @Column(name = "habilitado", nullable = false)
    @JsonProperty("habilitado")
    @Builder.Default
    private Boolean habilitado = Boolean.TRUE;
}
