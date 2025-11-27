package com.SebastianCornejo.Proyecto.Fullstack.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class SolicitudRecuperacionContrasena {
    @Schema(description="Correo del usuario", example="juan@example.com")
    private String correo;
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}

