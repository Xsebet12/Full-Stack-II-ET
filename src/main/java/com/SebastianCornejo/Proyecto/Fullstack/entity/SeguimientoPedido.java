package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "seguimiento_pedido", indexes = {
        @Index(name = "idx_seguimiento_created", columnList = "created_at")
})
public class SeguimientoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @ToString.Exclude
    private Venta venta;

    @Column(name = "estado_envio")
    private String estadoEnvio;

    @Column(name = "numero_seguimiento", length = 64)
    private String numeroSeguimiento;

    @Column(name = "fecha_estimada_entrega")
    private LocalDate fechaEstimadaEntrega;
}
