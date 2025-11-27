package com.SebastianCornejo.Proyecto.Fullstack.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RespuestaCarrito {
    private List<RespuestaItemCarrito> items;
    private BigDecimal total;
    private Integer cantidadItems;
}
