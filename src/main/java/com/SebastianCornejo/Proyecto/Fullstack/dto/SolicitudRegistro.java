package com.SebastianCornejo.Proyecto.Fullstack.dto;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Role;
import com.SebastianCornejo.Proyecto.Fullstack.entity.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Datos necesarios para registrar un nuevo usuario")
public class SolicitudRegistro {
    @NotBlank(message = "Los nombres son obligatorios")
    @Schema(description = "Nombres del usuario", example = "Juan")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Schema(description = "Apellidos del usuario", example = "Pérez")
    private String apellidos;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^\\d{8,}$", message = "El RUT debe contener solo dígitos y tener al menos 8 caracteres")
    @Schema(description = "RUT del usuario (sin dígito verificador)", example = "12345678")
    private String rut;

    @NotBlank(message = "El dígito verificador es obligatorio")
    @Size(min = 1, max = 1, message = "El dígito verificador debe tener longitud 1")
    @Pattern(regexp = "^[0-9Kk]$", message = "El dígito verificador debe ser 0-9 o K")
    @Schema(description = "Dígito verificador del RUT", example = "9")
    private String dv;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@(gmail\\.com|duocuc\\.cl)$", message = "Solo se permiten correos @gmail.com o @duocuc.cl")
    @Schema(description = "Correo electrónico (solo dominios gmail.com o duocuc.cl)", example = "juan@gmail.com")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña", example = "secret")
    private String contrasena;

    @NotBlank(message = "La dirección es obligatoria")
    @Schema(description = "Dirección postal", example = "Av. Siempre Viva 123")
    private String direccion;

    @NotNull(message = "La comuna (id) es obligatoria")
    @Schema(description = "ID de la comuna seleccionada", example = "1")
    private Integer comunaId; // id de la comuna seleccionada

    @Schema(description = "Rol del usuario (opcional). Si no se envía, será CLIENT", example = "CLIENT")
    private Role rol; // opcional: si no se envía, será CLIENT

    // Atributos opcionales para subtipo Cliente (si tipo=cliente)
    @Schema(description = "Tipo de cliente (opcional)", example = "DETALLE")
    private TipoCliente tipoCliente;

    @Schema(description = "Puntos de fidelización (opcional)", example = "0")
    private Integer puntosFidelizacion;

    @Schema(description = "Recibir promociones (opcional)", example = "true")
    private Boolean recibirPromos;

    @Schema(description = "Dirección de entrega (opcional)", example = "Calle Falsa 123, Dpto 4")
    private String direccionEntrega;

    @Schema(description = "Preferencias de comunicación (opcional)", example = "EMAIL")
    private String preferenciasComunicacion;

    @Schema(description = "Límite de crédito (opcional)", example = "100000.00")
    private BigDecimal limiteCredito;

    @Schema(description = "Frecuencia de compra mensual (opcional)", example = "3")
    private Integer frecuenciaCompra;

    // Atributos opcionales para subtipo Empleado (si tipo=empleado)
    @Schema(description = "Departamento del empleado (opcional)", example = "Ventas")
    private String departamento;

    @Schema(description = "Sueldo del empleado (opcional)", example = "800000.00")
    private BigDecimal sueldo;

    @Schema(description = "Fecha de contratación (opcional)", example = "2024-01-15")
    private LocalDate fechaContratacion;

    @Schema(description = "Fecha de nacimiento (opcional)", example = "1990-05-10")
    private LocalDate fechaNacimiento;

    @Schema(description = "Fecha de salida (opcional)", example = "2025-12-31")
    private LocalDate fechaSalida;

    @Schema(description = "Género (opcional)", example = "MASCULINO")
    private String genero;

    @Schema(description = "Nacionalidad (opcional)", example = "Chilena")
    private String nacionalidad;

    @Schema(description = "Número de cuenta bancaria (opcional)", example = "123456789")
    private String numeroCuentaBancaria;

    @Schema(description = "Tipo de contrato (opcional)", example = "INDEFINIDO")
    private String tipoContrato;

    @Schema(description = "Banco (opcional)", example = "BancoEstado")
    private String banco;

    @Schema(description = "Celular (opcional)", example = "+56912345678")
    private String celular;

    @Schema(description = "Cuenta activa (opcional)", example = "true")
    private Boolean cuentaActiva;
}
