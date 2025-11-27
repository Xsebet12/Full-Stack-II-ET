package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item_carrito", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"carrito_id", "producto_id"})
})
public class ItemCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    @Builder.Default
    private Instant creadoEn = Instant.now();

    @Transient
    public BigDecimal getSubtotal() {
        return precioUnitario != null && cantidad != null ? precioUnitario.multiply(BigDecimal.valueOf(cantidad)) : BigDecimal.ZERO;
    }

}
