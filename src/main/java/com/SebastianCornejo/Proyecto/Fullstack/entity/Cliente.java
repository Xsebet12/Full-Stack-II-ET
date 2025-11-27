package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes")
@DiscriminatorValue("CLIENTE")
public class Cliente extends Usuario {
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente")
    private TipoCliente tipoCliente;

    @Column(name = "limite_credito")
    private BigDecimal limiteCredito;

    @Column(name = "frecuencia_compra")
    private Integer frecuenciaCompra;

    @Column(name = "preferencias_comunicacion")
    private String preferenciasComunicacion;

    @Column(name = "direccion_entrega")
    private String direccionEntrega;

    @Column(name = "recibir_promos")
    private Boolean recibirPromos;

    @Column(name = "puntos_fidelizacion")
    private Integer puntosFidelizacion;
}
