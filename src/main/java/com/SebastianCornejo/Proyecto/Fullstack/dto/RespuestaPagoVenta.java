package com.SebastianCornejo.Proyecto.Fullstack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RespuestaPagoVenta {
    private Long id;
    private String estadoPago;
}
