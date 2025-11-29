package com.SebastianCornejo.Proyecto.Fullstack.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/contacto")
@Tag(name = "Contacto", description = "Recepción de mensajes de contacto")
public class ControladorContacto {
    private static final Logger log = LoggerFactory.getLogger(ControladorContacto.class);

    private final com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCorreo servicioCorreo;

    public ControladorContacto(com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCorreo servicioCorreo) {
        this.servicioCorreo = servicioCorreo;
    }

    @PostMapping
    @Operation(summary = "Enviar mensaje", description = "Recibe un mensaje y envía un correo al administrador")
    public ResponseEntity<Map<String,Object>> enviar(@RequestBody Map<String,String> body){
        String nombre = body.getOrDefault("nombre","-");
        String correo = body.getOrDefault("correo","-");
        String mensaje = body.getOrDefault("mensaje","-");
        log.info("CONTACTO nombre={} correo={} mensaje={} ", nombre, correo, mensaje);
        try {
            servicioCorreo.enviarContacto(nombre, correo, mensaje);
            return ResponseEntity.accepted().body(Map.of("status","ok","message","Mensaje enviado"));
        } catch (Exception ex) {
            log.warn("Fallo enviando correo de contacto: {}", ex.toString());
            return ResponseEntity.accepted().body(Map.of("status","ok","message","Mensaje recibido"));
        }
    }
}
