package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudRegistro;
import com.SebastianCornejo.Proyecto.Fullstack.dto.SolicitudActualizacionUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.dto.RespuestaUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Role;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Usuario;
import com.SebastianCornejo.Proyecto.Fullstack.entity.Comuna;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioUsuario;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioEmpleado;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioCliente;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioComuna;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioUsuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class ServicioUsuarioImpl implements ServicioUsuario {

    private final RepositorioUsuario userRepository;
    private final RepositorioComuna repositorioComuna;
    private final RepositorioEmpleado repositorioEmpleado;
    private final RepositorioCliente repositorioCliente;
    private final PasswordEncoder passwordEncoder;

    public ServicioUsuarioImpl(RepositorioUsuario userRepository, RepositorioComuna repositorioComuna, PasswordEncoder passwordEncoder,
                               RepositorioEmpleado repositorioEmpleado, RepositorioCliente repositorioCliente) {
        this.userRepository = userRepository;
        this.repositorioComuna = repositorioComuna;
        this.passwordEncoder = passwordEncoder;
        this.repositorioEmpleado = repositorioEmpleado;
        this.repositorioCliente = repositorioCliente;
    }

    @Override
    public RespuestaUsuario registerClient(SolicitudRegistro request) {
        // Validaciones de unicidad: correo y RUT no deben existir previamente.
        if (userRepository.existsByCorreo(request.getCorreo())) {
            throw new PeticionInvalidaException("El correo ya est\u00e1 registrado");
        }
        if (userRepository.existsByRut(request.getRut())) {
            throw new PeticionInvalidaException("El RUT ya est\u00e1 registrado");
        }
        // Registrar siempre como Cliente desde el flujo de cliente
        Usuario nuevo;
        com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente cli = new com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente();
        cli.setNombres(request.getNombres());
        cli.setApellidos(request.getApellidos());
        cli.setRut(request.getRut());
        cli.setDv(request.getDv());
        cli.setCorreo(request.getCorreo());
        cli.setContrasena(passwordEncoder.encode(request.getContrasena()));
        cli.setDireccion(request.getDireccion());
        if (request.getTelefono() != null) cli.setTelefono(request.getTelefono());
        // Subtipo cliente: usar valores del request si vienen, sino defaults
        cli.setTipoCliente(request.getTipoCliente() != null ? request.getTipoCliente() : com.SebastianCornejo.Proyecto.Fullstack.entity.TipoCliente.DETALLE);
        cli.setPuntosFidelizacion(request.getPuntosFidelizacion() != null ? request.getPuntosFidelizacion() : 0);
        if (request.getRecibirPromos() != null) cli.setRecibirPromos(request.getRecibirPromos());
        if (request.getDireccionEntrega() != null) cli.setDireccionEntrega(request.getDireccionEntrega());
        if (request.getPreferenciasComunicacion() != null) cli.setPreferenciasComunicacion(request.getPreferenciasComunicacion());
        if (request.getLimiteCredito() != null) cli.setLimiteCredito(request.getLimiteCredito());
        if (request.getFrecuenciaCompra() != null) cli.setFrecuenciaCompra(request.getFrecuenciaCompra());
        nuevo = cli;
        // Asignar comuna (obligatoria por DTO)
        Comuna comuna = repositorioComuna.findById(Objects.requireNonNull(request.getComunaId()))
                .orElseThrow(() -> new PeticionInvalidaException("Comuna no encontrada"));
        nuevo.setComuna(comuna);
        if (request.getTelefono() != null) nuevo.setTelefono(request.getTelefono());

        Usuario saved = Objects.requireNonNull(userRepository.save(nuevo));
        return toDto(saved);
    }

    @Override
    public RespuestaUsuario register(SolicitudRegistro request, String tipo) {
        if (userRepository.existsByCorreo(request.getCorreo())) {
            throw new PeticionInvalidaException("El correo ya está registrado");
        }
        if (userRepository.existsByRut(request.getRut())) {
            throw new PeticionInvalidaException("El RUT ya está registrado");
        }
        Usuario nuevo;
        String t = tipo != null ? tipo.trim().toUpperCase() : null;
        if ("EMPLEADO".equals(t) || "EMPLEADOS".equals(t)) {
            if (request.getCelular() != null && repositorioEmpleado.existsByCelular(request.getCelular())) {
                throw new PeticionInvalidaException("El número ya está registrado");
            }
            com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado emp = new com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado();
            emp.setNombres(request.getNombres());
            emp.setApellidos(request.getApellidos());
            emp.setRut(request.getRut());
            emp.setDv(request.getDv());
            emp.setCorreo(request.getCorreo());
            emp.setContrasena(passwordEncoder.encode(request.getContrasena()));
            emp.setDireccion(request.getDireccion());
            // Para empleado: usar rol del request si es ADMIN, sino ADMIN por defecto
            emp.setRol(com.SebastianCornejo.Proyecto.Fullstack.entity.Role.ADMIN);
            if (request.getDepartamento() != null) emp.setDepartamento(request.getDepartamento());
            if (request.getSueldo() != null) emp.setSueldo(request.getSueldo());
            if (request.getFechaContratacion() != null) emp.setFechaContratacion(request.getFechaContratacion());
            if (request.getFechaNacimiento() != null) emp.setFechaNacimiento(request.getFechaNacimiento());
            if (request.getFechaSalida() != null) emp.setFechaSalida(request.getFechaSalida());
            if (request.getGenero() != null) emp.setGenero(request.getGenero());
            if (request.getNacionalidad() != null) emp.setNacionalidad(request.getNacionalidad());
            if (request.getNumeroCuentaBancaria() != null) emp.setNumeroCuentaBancaria(request.getNumeroCuentaBancaria());
            if (request.getTipoContrato() != null) emp.setTipoContrato(request.getTipoContrato());
            if (request.getBanco() != null) emp.setBanco(request.getBanco());
            if (request.getCelular() != null) emp.setCelular(request.getCelular());
            if (request.getCuentaActiva() != null) emp.setCuentaActiva(request.getCuentaActiva());
            nuevo = emp;
        } else if ("CLIENTE".equals(t) || "CLIENTES".equals(t)) {
            com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente cli = new com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente();
            cli.setNombres(request.getNombres());
            cli.setApellidos(request.getApellidos());
            cli.setRut(request.getRut());
            cli.setDv(request.getDv());
            cli.setCorreo(request.getCorreo());
            cli.setContrasena(passwordEncoder.encode(request.getContrasena()));
            cli.setDireccion(request.getDireccion());
            cli.setTipoCliente(request.getTipoCliente() != null ? request.getTipoCliente() : com.SebastianCornejo.Proyecto.Fullstack.entity.TipoCliente.DETALLE);
            cli.setPuntosFidelizacion(request.getPuntosFidelizacion() != null ? request.getPuntosFidelizacion() : 0);
            if (request.getRecibirPromos() != null) cli.setRecibirPromos(request.getRecibirPromos());
            if (request.getDireccionEntrega() != null) cli.setDireccionEntrega(request.getDireccionEntrega());
            if (request.getPreferenciasComunicacion() != null) cli.setPreferenciasComunicacion(request.getPreferenciasComunicacion());
            if (request.getLimiteCredito() != null) cli.setLimiteCredito(request.getLimiteCredito());
            if (request.getFrecuenciaCompra() != null) cli.setFrecuenciaCompra(request.getFrecuenciaCompra());
            nuevo = cli;
        } else {
            return registerClient(request);
        }
        Comuna comuna = repositorioComuna.findById(Objects.requireNonNull(request.getComunaId()))
                .orElseThrow(() -> new PeticionInvalidaException("Comuna no encontrada"));
        nuevo.setComuna(comuna);
        Usuario saved = Objects.requireNonNull(userRepository.save(nuevo));
        return toDto(saved);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return userRepository.existsByCorreo(correo);
    }

    @Override
    public RespuestaUsuario findById(Long id) {
        Usuario u = userRepository.findById(Objects.requireNonNull(id)).orElseThrow(() -> new com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException("Usuario no encontrado"));
        return toDto(u);
    }

    @Override
    public RespuestaUsuario update(Long id, SolicitudActualizacionUsuario request) {
        Usuario u = userRepository.findById(Objects.requireNonNull(id)).orElseThrow(() -> new com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException("Usuario no encontrado"));
        if (request.getNombres() != null) u.setNombres(request.getNombres().trim());
        if (request.getApellidos() != null) u.setApellidos(request.getApellidos().trim());
        if (request.getRut() != null) {
            String newRut = request.getRut().trim();
            if (!newRut.equalsIgnoreCase(u.getRut()) && userRepository.existsByRut(newRut)) {
                throw new com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException("El RUT ya está registrado");
            }
            u.setRut(newRut);
        }
        if (request.getDv() != null) u.setDv(request.getDv().trim());
        if (request.getCorreo() != null) {
            String newMail = request.getCorreo().trim();
            if (!newMail.equalsIgnoreCase(u.getCorreo()) && userRepository.existsByCorreo(newMail)) {
                throw new com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException("El correo ya está registrado");
            }
            u.setCorreo(newMail);
        }
        if (request.getDireccion() != null) u.setDireccion(request.getDireccion().trim());
        if (request.getTelefono() != null) u.setTelefono(request.getTelefono().trim());
        if (request.getRol() != null && u instanceof com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado) {
            ((com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado) u).setRol(request.getRol());
        }
        if (request.getEnabled() != null) u.setHabilitado(request.getEnabled());
        if (request.getComunaId() != null) {
        Comuna comuna = repositorioComuna.findById(Objects.requireNonNull(request.getComunaId()))
                    .orElseThrow(() -> new com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException("Comuna no encontrada"));
            u.setComuna(comuna);
        }
        if (u instanceof com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente) {
            com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente c = (com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente) u;
            if (request.getTipoCliente() != null) c.setTipoCliente(request.getTipoCliente());
            if (request.getPuntosFidelizacion() != null) c.setPuntosFidelizacion(request.getPuntosFidelizacion());
            if (request.getRecibirPromos() != null) c.setRecibirPromos(request.getRecibirPromos());
            if (request.getDireccionEntrega() != null) c.setDireccionEntrega(request.getDireccionEntrega());
            if (request.getPreferenciasComunicacion() != null) c.setPreferenciasComunicacion(request.getPreferenciasComunicacion());
            if (request.getLimiteCredito() != null) c.setLimiteCredito(request.getLimiteCredito());
            if (request.getFrecuenciaCompra() != null) c.setFrecuenciaCompra(request.getFrecuenciaCompra());
        }
        if (u instanceof com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado) {
            com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado e2 = (com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado) u;
            if (request.getCelular() != null) {
                String newCel = request.getCelular().trim();
                java.util.Optional<com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado> other = repositorioEmpleado.findByCelular(newCel);
                if (other.isPresent() && !other.get().getId().equals(e2.getId())) {
                    throw new com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException("El número ya está registrado");
                }
                e2.setCelular(newCel);
            }
        }
        if (u instanceof com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado) {
            com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado e = (com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado) u;
            if (request.getDepartamento() != null) e.setDepartamento(request.getDepartamento());
            if (request.getSueldo() != null) e.setSueldo(request.getSueldo());
            if (request.getFechaContratacion() != null) e.setFechaContratacion(request.getFechaContratacion());
            if (request.getFechaNacimiento() != null) e.setFechaNacimiento(request.getFechaNacimiento());
            if (request.getFechaSalida() != null) e.setFechaSalida(request.getFechaSalida());
            if (request.getGenero() != null) e.setGenero(request.getGenero());
            if (request.getNacionalidad() != null) e.setNacionalidad(request.getNacionalidad());
            if (request.getNumeroCuentaBancaria() != null) e.setNumeroCuentaBancaria(request.getNumeroCuentaBancaria());
            if (request.getTipoContrato() != null) e.setTipoContrato(request.getTipoContrato());
            if (request.getBanco() != null) e.setBanco(request.getBanco());
            if (request.getCelular() != null) e.setCelular(request.getCelular());
            if (request.getCuentaActiva() != null) e.setCuentaActiva(request.getCuentaActiva());
        }
        Usuario saved = Objects.requireNonNull(userRepository.save(u));
        return toDto(saved);
    }

    @Override
    public void disable(Long id) {
        Usuario u = userRepository.findById(Objects.requireNonNull(id)).orElseThrow(() -> new com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException("Usuario no encontrado"));
        u.setHabilitado(false);
        userRepository.save(u);
    }

    private RespuestaUsuario toDto(Usuario saved) {
        RespuestaUsuario.RespuestaUsuarioBuilder<?, ?> b = RespuestaUsuario.builder()
                .id(saved.getId())
                .nombres(saved.getNombres())
                .apellidos(saved.getApellidos())
                .rut(saved.getRut())
                .dv(saved.getDv())
                .correo(saved.getCorreo())
                .telefono(saved.getTelefono())
                .direccion(saved.getDireccion())
                .comuna(saved.getComuna() != null ? saved.getComuna().getNomComuna() : null)
                .region(saved.getComuna() != null && saved.getComuna().getRegion() != null ? saved.getComuna().getRegion().getNomRegion() : null)
                .comunaId(saved.getComuna() != null ? saved.getComuna().getIdComuna() : null)
                .regionId(saved.getComuna() != null && saved.getComuna().getRegion() != null ? saved.getComuna().getRegion().getIdRegion() : null)
                .enabled(saved.getHabilitado())
                .createdAt(saved.getCreadoEn());
        if (saved instanceof com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado e) {
            b.rol(e.getRol())
             .departamento(e.getDepartamento())
             .sueldo(e.getSueldo())
             .fechaContratacion(e.getFechaContratacion())
             .fechaNacimiento(e.getFechaNacimiento())
             .fechaSalida(e.getFechaSalida())
             .genero(e.getGenero())
             .nacionalidad(e.getNacionalidad())
             .numeroCuentaBancaria(e.getNumeroCuentaBancaria())
             .tipoContrato(e.getTipoContrato())
             .banco(e.getBanco())
             .celular(e.getCelular())
             .cuentaActiva(e.getCuentaActiva());
        }
        if (saved instanceof com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente c) {
            b.tipoCliente(c.getTipoCliente())
             .puntosFidelizacion(c.getPuntosFidelizacion())
             .recibirPromos(c.getRecibirPromos())
             .direccionEntrega(c.getDireccionEntrega())
             .preferenciasComunicacion(c.getPreferenciasComunicacion())
             .limiteCredito(c.getLimiteCredito())
             .frecuenciaCompra(c.getFrecuenciaCompra());
        }
        return b.build();
    }

    @Override
    public RespuestaUsuario setHabilitado(Long id, Boolean habilitado) {
        if (habilitado == null) {
            throw new com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException("El valor de 'habilitado' es requerido");
        }
        Usuario u = userRepository.findById(Objects.requireNonNull(id)).orElseThrow(() -> new com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException("Usuario no encontrado"));
        u.setHabilitado(habilitado);
        return toDto(Objects.requireNonNull(userRepository.save(u)));
    }

    @Override
    public void cambiarContrasena(String correo, String antigua, String nueva) {
        Usuario u = userRepository.findByCorreo(Objects.requireNonNull(correo))
                .orElseThrow(() -> new com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException("Usuario no encontrado"));
        if (antigua == null || nueva == null || antigua.isBlank() || nueva.isBlank()) {
            throw new PeticionInvalidaException("Contraseñas inválidas");
        }
        if (!passwordEncoder.matches(antigua, u.getContrasena())) {
            throw new PeticionInvalidaException("La contraseña actual no coincide");
        }
        u.setContrasena(passwordEncoder.encode(nueva));
        userRepository.save(u);
    }

    @Override
    public void resetContrasena(String correo, String nueva) {
        Usuario u = userRepository.findByCorreo(Objects.requireNonNull(correo))
                .orElseThrow(() -> new com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException("Usuario no encontrado"));
        if (nueva == null || nueva.isBlank()) {
            throw new PeticionInvalidaException("Contraseña inválida");
        }
        u.setContrasena(passwordEncoder.encode(nueva));
        userRepository.save(u);
    }

    @Override
    public java.util.List<RespuestaUsuario> listar(Boolean habilitado, String tipo) {
        java.util.List<RespuestaUsuario> out = new java.util.ArrayList<>();
        String t = tipo != null ? tipo.trim().toUpperCase() : null;
        if (t == null) {
            java.util.List<com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado> empleados =
                    (habilitado == null) ? repositorioEmpleado.findAll() : repositorioEmpleado.findByHabilitado(habilitado);
            java.util.List<com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente> clientes =
                    (habilitado == null) ? repositorioCliente.findAll() : repositorioCliente.findByHabilitado(habilitado);
            for (Usuario u : empleados) out.add(toDto(u));
            for (Usuario u : clientes) out.add(toDto(u));
            return out;
        }
        if ("EMPLEADOS".equals(t) || "EMPLEADO".equals(t)) {
            java.util.List<com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado> empleados =
                    (habilitado == null) ? repositorioEmpleado.findAll() : repositorioEmpleado.findByHabilitado(habilitado);
            for (Usuario u : empleados) out.add(toDto(u));
            return out;
        }
        if ("CLIENTES".equals(t) || "CLIENTE".equals(t)) {
            java.util.List<com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente> clientes =
                    (habilitado == null) ? repositorioCliente.findAll() : repositorioCliente.findByHabilitado(habilitado);
            for (Usuario u : clientes) out.add(toDto(u));
            return out;
        }
        // Si tipo desconocido, devolver unión como fallback
        java.util.List<com.SebastianCornejo.Proyecto.Fullstack.entity.Empleado> empleados =
                (habilitado == null) ? repositorioEmpleado.findAll() : repositorioEmpleado.findByHabilitado(habilitado);
        java.util.List<com.SebastianCornejo.Proyecto.Fullstack.entity.Cliente> clientes =
                (habilitado == null) ? repositorioCliente.findAll() : repositorioCliente.findByHabilitado(habilitado);
        for (Usuario u : empleados) out.add(toDto(u));
        for (Usuario u : clientes) out.add(toDto(u));
        return out;
    }
}
