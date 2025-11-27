package com.SebastianCornejo.Proyecto.Fullstack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RespuestaAutenticacion {
    private String token;
    @JsonProperty("tokenType")
    private String tipoToken;
    @JsonProperty("expiresIn")
    private long expiraEn;
}
