package com.SebastianCornejo.Proyecto.Fullstack.dto;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Role;
import com.SebastianCornejo.Proyecto.Fullstack.entity.TipoCliente;
import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Campos para actualizar un usuario (ADMIN)")
public class SolicitudActualizacionUsuario {
    private String nombres; // opcional, si viene se valida longitud > 0 del lado servicio si se requiere
    private String apellidos; // opcional

    // Si se envía rut, debe ser sólo dígitos y de largo mínimo 8
    @Pattern(regexp = "^\\d{8,}$", message = "El RUT debe contener solo dígitos y tener al menos 8 caracteres")
    private String rut; // opcional

    // Si se envía dv, debe ser 1 char 0-9 o K/k
    @Size(min = 1, max = 1, message = "El dígito verificador debe tener longitud 1")
    @Pattern(regexp = "^[0-9Kk]$", message = "El dígito verificador debe ser 0-9 o K")
    private String dv; // opcional

    // Si se envía correo, debe tener formato y dominio permitido
    @Email(message = "El correo no tiene un formato válido")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@(gmail\\.com|duocuc\\.cl)$", message = "Solo se permiten correos @gmail.com o @duocuc.cl")
    private String correo; // opcional

    private String direccion; // opcional
    @Size(max = 15, message = "El teléfono debe tener máximo 15 caracteres")
    @Pattern(regexp = "^[\\d\\s()+-]{7,15}$", message = "Formato permitido: dígitos, espacios, +, -, ()")
    private String telefono; // opcional
    private Role rol; // opcional
    private Boolean enabled; // mapea a 'habilitado'
    private Integer comunaId; // opcional: cambiar comuna
    private TipoCliente tipoCliente;
    private Integer puntosFidelizacion;
    private Boolean recibirPromos;
    private String direccionEntrega;
    private String preferenciasComunicacion;
    private BigDecimal limiteCredito;
    private Integer frecuenciaCompra;

    // Campos de empleado
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
