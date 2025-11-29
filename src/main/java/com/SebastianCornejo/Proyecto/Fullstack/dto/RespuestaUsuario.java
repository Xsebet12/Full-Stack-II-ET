package com.SebastianCornejo.Proyecto.Fullstack.dto;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Role;
import com.SebastianCornejo.Proyecto.Fullstack.entity.TipoCliente;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RespuestaUsuario {
    private Long id;
    private String nombres;
    private String apellidos;
    private String rut;
    private String dv;
    private String correo;
    private String telefono;
    private Role rol;
    private String direccion;
    private String comuna;
    private String region;
    private Integer comunaId;
    private Integer regionId;
    private Boolean enabled;
    private Instant createdAt;
    private TipoCliente tipoCliente;
    private Integer puntosFidelizacion;
    private Boolean recibirPromos;
    private String direccionEntrega;
    private String preferenciasComunicacion;
    private BigDecimal limiteCredito;
    private Integer frecuenciaCompra;

    // Datos de empleado
    private String departamento;
    private BigDecimal sueldo;
    private LocalDate fechaContratacion;
    private LocalDate fechaNacimiento;
    private LocalDate fechaSalida;
    private String genero;
    private String nacionalidad;
    private String numeroCuentaBancaria;
    private String tipoContrato;
    private String banco;
    private String celular;
    private Boolean cuentaActiva;
}
