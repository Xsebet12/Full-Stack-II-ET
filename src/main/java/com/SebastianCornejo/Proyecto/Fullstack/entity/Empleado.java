package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "empleados")
@DiscriminatorValue("EMPLEADO")
public class Empleado extends Usuario {
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Role rol;

    @Column
    private String departamento;

    @Column
    private BigDecimal sueldo;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column
    private String genero;

    @Column
    private String nacionalidad;

    @Column(name = "numero_cuenta_bancaria")
    private String numeroCuentaBancaria;

    @Column(name = "tipo_contrato")
    private String tipoContrato;

    @Column(name = "ultimo_acceso")
    private Instant ultimoAcceso;

    @Column
    private String banco;

    @Column
    private String celular;

    @Column(name = "cuenta_activa")
    private Boolean cuentaActiva;
}
