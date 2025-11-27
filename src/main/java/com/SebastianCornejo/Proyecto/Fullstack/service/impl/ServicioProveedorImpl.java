package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.entity.Proveedor;
import com.SebastianCornejo.Proyecto.Fullstack.entity.EstadoProveedor;
import com.SebastianCornejo.Proyecto.Fullstack.entity.ContactoProveedor;
import com.SebastianCornejo.Proyecto.Fullstack.exception.PeticionInvalidaException;
import com.SebastianCornejo.Proyecto.Fullstack.exception.RecursoNoEncontradoException;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioContactoProveedor;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProveedor;
import com.SebastianCornejo.Proyecto.Fullstack.repository.RepositorioProducto;
import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioProveedor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ServicioProveedorImpl implements ServicioProveedor {

    private final RepositorioProveedor repositorioProveedor;
    //private final RepositorioContactoProveedor repositorioContactoProveedor;
    private final RepositorioProducto repositorioProducto;

    public ServicioProveedorImpl(RepositorioProveedor repositorioProveedor,
                                 RepositorioContactoProveedor repositorioContactoProveedor,
                                 RepositorioProducto repositorioProducto) {
        this.repositorioProveedor = repositorioProveedor;
        //this.repositorioContactoProveedor = repositorioContactoProveedor;
        this.repositorioProducto = repositorioProducto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> findAll() {
    return repositorioProveedor.findAll();
    }

    @Override
    @Transactional(readOnly = true)
        public List<Proveedor> findAllActivos() {
    return repositorioProveedor.findByEstado(EstadoProveedor.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> findAllPorEstado(Boolean activo) {
        if (activo == null) return findAll();
        return repositorioProveedor.findByEstado(Boolean.TRUE.equals(activo) ? EstadoProveedor.ACTIVO : EstadoProveedor.INACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor findById(Long id) {
    return repositorioProveedor.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id " + id));
    }

    @Override
    public Proveedor create(Proveedor proveedor) {
        if (proveedor == null) throw new PeticionInvalidaException("Proveedor inválido");
        // Si el estado no viene, establecer ACTIVO por defecto
        if (proveedor.getEstado() == null) {
            proveedor.setEstado(EstadoProveedor.ACTIVO);
        }
        if (repositorioProveedor.existsByCompanyNameIgnoreCase(proveedor.getCompanyName())) {
            throw new PeticionInvalidaException("Ya existe un proveedor con el nombre '" + proveedor.getCompanyName() + "'");
        }
    Set<ContactoProveedor> contactos = proveedor.getContacts();
        if (contactos != null && !contactos.isEmpty()) {
            for (ContactoProveedor contacto : contactos) {
                    // ContactoProveedor.proveedor es de tipo Proveedor en el proyecto, asignar directamente
                    contacto.setProveedor(proveedor);
                }
        }
        try {
            return Objects.requireNonNull(repositorioProveedor.save(proveedor));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al crear proveedor: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Proveedor update(Long id, Proveedor proveedorUpdate) {
        Proveedor existing = findById(id);
        // companyName con validación y trim
        if (proveedorUpdate.getCompanyName() != null && !proveedorUpdate.getCompanyName().isBlank()) {
            String newName = proveedorUpdate.getCompanyName().trim();
            if (!newName.equalsIgnoreCase(existing.getCompanyName()) && repositorioProveedor.existsByCompanyNameIgnoreCase(newName)) {
                throw new PeticionInvalidaException("Ya existe un proveedor con el nombre '" + newName + "'");
            }
            existing.setCompanyName(newName);
        }
         // serviceType/url/phone/logo con trim si vienen
        if (proveedorUpdate.getServiceType() != null) existing.setServiceType(proveedorUpdate.getServiceType().trim());
        if (proveedorUpdate.getUrl() != null) existing.setUrl(proveedorUpdate.getUrl() == null ? null : proveedorUpdate.getUrl().trim());
        if (proveedorUpdate.getPhone() != null) existing.setPhone(proveedorUpdate.getPhone() == null ? null : proveedorUpdate.getPhone().trim());
        if (proveedorUpdate.getLogoUrl() != null) existing.setLogoUrl(proveedorUpdate.getLogoUrl().trim());
        // permitir actualizar el estado (activo/inactivo)
        if (proveedorUpdate.getEstado() != null) existing.setEstado(proveedorUpdate.getEstado());
        try {
            return Objects.requireNonNull(repositorioProveedor.saveAndFlush(existing));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al actualizar proveedor: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        Proveedor existing = findById(id);
    long count = repositorioProducto.countByProveedorId(id);
        if (count > 0) {
            throw new PeticionInvalidaException("No se puede eliminar el proveedor: tiene " + count + " productos asociados");
        }
        repositorioProveedor.delete(Objects.requireNonNull(existing));
    }

    @Override
    public Proveedor setEstado(Long id, Boolean activo) {
        Proveedor existing = findById(id);
        if (activo == null) {
            throw new PeticionInvalidaException("El valor de 'activo' es requerido");
        }
    EstadoProveedor nuevo = activo
        ? EstadoProveedor.ACTIVO
        : EstadoProveedor.INACTIVO;
        existing.setEstado(nuevo);
        try {
            return Objects.requireNonNull(repositorioProveedor.save(existing));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al cambiar estado: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Proveedor addContact(Long proveedorId, ContactoProveedor contacto) {
        Proveedor proveedor = findById(proveedorId);
        validateContact(contacto);
        if (proveedor.getContacts() == null) {
            proveedor.setContacts(new LinkedHashSet<>());
        }
    // set proveedor (Proveedor) on the contacto
    contacto.setProveedor(proveedor);
        // Si este nuevo contacto viene marcado como principal, desmarcar los demás
        if (Boolean.TRUE.equals(contacto.getPrincipal())) {
            for (ContactoProveedor c : proveedor.getContacts()) {
                c.setPrincipal(false);
            }
        }
        proveedor.getContacts().add(contacto);
        try {
            return Objects.requireNonNull(repositorioProveedor.save(proveedor));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al agregar contacto: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Proveedor updateContact(Long proveedorId, Long contactoId, ContactoProveedor actualizacion) {
    Proveedor proveedor = findById(proveedorId);
        ContactoProveedor target = getContactFromProvider(proveedor, contactoId);
        if (actualizacion.getName() != null) target.setName(actualizacion.getName());
        if (actualizacion.getPhone() != null) target.setPhone(actualizacion.getPhone());
        if (actualizacion.getRole() != null) target.setRole(actualizacion.getRole());
        if (actualizacion.getEmail() != null) target.setEmail(actualizacion.getEmail());
        if (Boolean.TRUE.equals(actualizacion.getPrincipal())) {
            // Si se marca como principal, desmarcar los demás
            for (ContactoProveedor c : proveedor.getContacts()) {
                c.setPrincipal(c.getId() != null && c.getId().equals(contactoId));
            }
        } else if (actualizacion.getId() != null && Boolean.FALSE.equals(actualizacion.getPrincipal())) {
            // Permitir desmarcar explícitamente como no-principal
            target.setPrincipal(false);
        }
        try {
            return Objects.requireNonNull(repositorioProveedor.save(Objects.requireNonNull(proveedor)));
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al actualizar contacto: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Proveedor removeContact(Long proveedorId, Long contactoId) {
        Proveedor proveedor = findById(proveedorId);
    ContactoProveedor target = getContactFromProvider(proveedor, contactoId);
        boolean removed = proveedor.getContacts().remove(target);
        if (!removed) {
            throw new PeticionInvalidaException("No se pudo eliminar el contacto del conjunto");
        }
        try {
            return repositorioProveedor.save(proveedor);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al eliminar contacto: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public Proveedor updateLogo(Long id, String urlLogo) {
        Proveedor existing = findById(id);
        if (urlLogo == null || urlLogo.isBlank()) {
            throw new PeticionInvalidaException("URL de logo inválida");
        }
        existing.setLogoUrl(urlLogo);
        try {
            return repositorioProveedor.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al actualizar logo: " + e.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public List<ContactoProveedor> listarContactos(Long proveedorId) {
        Proveedor proveedor = findById(proveedorId);
        Set<ContactoProveedor> contactos = proveedor.getContacts();
        if (contactos == null || contactos.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(contactos);
    }

    private void validateContact(ContactoProveedor contacto) {
    if (contacto == null) throw new PeticionInvalidaException("Contacto inválido");
    if (contacto.getName() == null || contacto.getName().isBlank()) throw new PeticionInvalidaException("El nombre del contacto es obligatorio");
    if (contacto.getPhone() == null || contacto.getPhone().isBlank()) throw new PeticionInvalidaException("El teléfono del contacto es obligatorio");
    if (contacto.getRole() == null || contacto.getRole().isBlank()) throw new PeticionInvalidaException("El cargo del contacto es obligatorio");
    if (contacto.getEmail() == null || contacto.getEmail().isBlank()) throw new PeticionInvalidaException("El correo del contacto es obligatorio");
    }

    private ContactoProveedor getContactFromProvider(Proveedor proveedor, Long contactoId) {
        if (proveedor.getContacts() == null || proveedor.getContacts().isEmpty()) {
            throw new RecursoNoEncontradoException("El proveedor no tiene contactos");
        }
        for (ContactoProveedor c : proveedor.getContacts()) {
            if (c.getId() != null && c.getId().equals(contactoId)) {
                return c;
            }
        }
    throw new RecursoNoEncontradoException("Contacto no encontrado con id " + contactoId);
    }

    @Override
    public Proveedor setPrincipal(Long proveedorId, Long contactoId) {
        Proveedor proveedor = findById(proveedorId);
        ContactoProveedor target = getContactFromProvider(proveedor, contactoId);
        for (ContactoProveedor c : proveedor.getContacts()) {
            c.setPrincipal(c == target);
        }
        try {
            return repositorioProveedor.save(proveedor);
        } catch (DataIntegrityViolationException e) {
            throw new PeticionInvalidaException("Violación de integridad de datos al establecer contacto principal: " + e.getMostSpecificCause().getMessage());
        }
    }
}
