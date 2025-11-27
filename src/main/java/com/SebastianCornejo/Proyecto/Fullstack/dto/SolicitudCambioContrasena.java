package com.SebastianCornejo.Proyecto.Fullstack.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class SolicitudCambioContrasena {
    @Schema(description="Contraseña actual", example="oldSecret")
    private String antigua;
    @Schema(description="Nueva contraseña", example="newSecret123")
    private String nueva;

    public String getAntigua() { return antigua; }
    public void setAntigua(String antigua) { this.antigua = antigua; }
    public String getNueva() { return nueva; }
    public void setNueva(String nueva) { this.nueva = nueva; }
}

