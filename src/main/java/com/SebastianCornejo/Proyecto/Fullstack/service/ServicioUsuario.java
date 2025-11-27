package com.SebastianCornejo.Proyecto.Fullstack.service;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudRegistro;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaUsuario;

public interface ServicioUsuario {
    /**
     * Registra un nuevo usuario con rol CLIENT por defecto (si no se especifica).
     * Valida unicidad de correo y RUT y retorna un DTO con los datos creados.
     */
    RespuestaUsuario registerClient(SolicitudRegistro request);

    RespuestaUsuario register(SolicitudRegistro request, String tipo);

    /**
     * Comprueba si existe un usuario con el correo indicado
     */
    boolean existsByCorreo(String correo);

    /**
     * Obtiene un usuario por id y lo devuelve como DTO de respuesta
     */
    RespuestaUsuario findById(Long id);

    /**
     * Actualiza datos del usuario (ADMIN) y devuelve el DTO actualizado
     */
    RespuestaUsuario update(Long id, com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudActualizacionUsuario request);

    /**
     * Deshabilita (soft delete) al usuario
     */
    void disable(Long id);

    /**
     * Cambia el estado habilitado del usuario (true/false) y devuelve el DTO
     */
    RespuestaUsuario setHabilitado(Long id, Boolean habilitado);

    /**
     * Cambia la contraseña del usuario identificado por su correo.
     */
    void cambiarContrasena(String correo, String antigua, String nueva);

    /**
     * Resetea la contraseña del usuario sin requerir la anterior (uso en recuperación)
     */
    void resetContrasena(String correo, String nueva);

    /**
     * Lista usuarios aplicando filtros opcionales de habilitado y tipo (EMPLEADO/CLIENTE).
     * Si tipo es null, retorna la unión de empleados y clientes.
     */
    java.util.List<RespuestaUsuario> listar(Boolean habilitado, String tipo);
}
