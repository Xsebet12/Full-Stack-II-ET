package com.SebastianCornejo.Proyecto.Fullstack.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RespuestaItemCarrito {
    private Long productoId;
    private String nombre;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal subtotal;
    private Integer stockDisponible;
}
    
