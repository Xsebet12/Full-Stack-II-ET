package com.SebastianCornejo.Proyecto.Fullstack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class OrdenResumen {
    private Long id;
    private Instant fecha;
    private BigDecimal total;
    private String estadoPago;
    private String estadoEnvio;
    private String numeroSeguimiento;
    private Long numeroBoleta;
}
