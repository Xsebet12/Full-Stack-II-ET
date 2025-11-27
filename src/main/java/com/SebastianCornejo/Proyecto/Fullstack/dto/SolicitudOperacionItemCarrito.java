package com.SebastianCornejo.Proyecto.Fullstack.dto;

import lombok.Data;

@Data
public class SolicitudOperacionItemCarrito {
    private Long productoId;
    private Integer cantidad; // cantidad a agregar o quitar
}
