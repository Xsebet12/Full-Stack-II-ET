package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "boletas", indexes = {
        @Index(name = "idx_boleta_numero", columnList = "numero", unique = true)
})
public class Boleta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "numero", nullable = false, unique = true)
    private Long numero;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    @ToString.Exclude
    private Venta venta;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Column(name = "monto_neto", precision = 19, scale = 2, nullable = false)
    private BigDecimal montoNeto;

    @Column(name = "monto_iva", precision = 19, scale = 2, nullable = false)
    private BigDecimal montoIva;

    @Column(name = "monto_total", precision = 19, scale = 2, nullable = false)
    private BigDecimal montoTotal;
}

