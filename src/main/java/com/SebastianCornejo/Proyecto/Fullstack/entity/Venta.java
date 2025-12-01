package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Column(name = "total", precision = 19, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "canal")
    private String canal;

    @Column(name = "estado_pago")
    private String estadoPago;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DetalleVenta> detalles;
}
