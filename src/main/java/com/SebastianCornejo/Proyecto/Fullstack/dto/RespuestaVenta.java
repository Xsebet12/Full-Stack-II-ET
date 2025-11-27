package com.SebastianCornejo.Proyecto.Fullstack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RespuestaVenta {
    private Long id;
    private BigDecimal total;
    private Integer cantidadItems;
    private String mensaje;
}

