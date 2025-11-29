package com.SebastianCornejo.Proyecto.Fullstack.service.impl;

import com.SebastianCornejo.Proyecto.Fullstack.service.ServicioCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class ServicioCorreoImpl implements ServicioCorreo {
    private static final Logger log = LoggerFactory.getLogger(ServicioCorreoImpl.class);

    private final JavaMailSender mailSender;

    @Value("${contact.dest:xsebet@gmail.com}")
    private String destinatarioContacto;

    @Value("${spring.mail.username:}")
    private String remitente;

    @Value("${contact.cc:}")
    private String destinatarioCc;

    public ServicioCorreoImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarContacto(String nombre, String correo, String mensaje) {
        try {
            MimeMessage mm = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mm, false, "UTF-8");
            if (remitente != null && !remitente.isBlank()) helper.setFrom(remitente);
            if (correo != null && !correo.isBlank()) helper.setReplyTo(correo);
            helper.setTo(destinatarioContacto);
            if (destinatarioCc != null && !destinatarioCc.isBlank()) helper.addCc(destinatarioCc);
            helper.setSubject("Nuevo mensaje de contacto");
            StringBuilder body = new StringBuilder();
            body.append("Has recibido un nuevo mensaje de contacto.\n\n");
            body.append("Nombre: ").append(nombre == null ? "-" : nombre).append('\n');
            body.append("Correo: ").append(correo == null ? "-" : correo).append('\n');
            body.append("Mensaje:\n").append(mensaje == null ? "-" : mensaje).append('\n');
            helper.setText(body.toString());
            mailSender.send(mm);
            log.info("Correo de contacto enviado a {} desde {}", destinatarioContacto, correo);
        } catch (MessagingException | RuntimeException ex) {
            log.error("No se pudo enviar correo de contacto", ex);
        }
    }
}
