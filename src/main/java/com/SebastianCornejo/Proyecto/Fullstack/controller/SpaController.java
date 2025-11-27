package com.SebastianCornejo.Proyecto.Fullstack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {
    @RequestMapping({"/", "/home", "/catalogo", "/producto/**", "/login", "/registrarCuenta", "/carrito", "/perfil", "/preferencias", "/sobre-nosotros", "/contacto", "/locales", "/blog", "/politicas", "/youkaADMIN/**", "/admin/**"})
    public String index() {
        return "forward:/index.html";
    }
}
