package com.SebastianCornejo.Proyecto.Fullstack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Configuración para exponer archivos estáticos (imágenes) desde el
        // directorio de uploads en disco. Esto permite que URLs como
        // http://<host>:<port>/images/<filename> sirvan el archivo directamente.
        //
        // Comportamiento:
        // - Si la variable de entorno UPLOADS_DIR está definida, usa UPLOADS_DIR/images
        // - En caso contrario, usa el directorio './uploads/images' relativo al working dir
        // - Registra un resource handler para '/images/**' apuntando a 'file:<path>'
        String uploads = System.getenv("UPLOADS_DIR");
        Path imagesDir;
        if (uploads != null && !uploads.isBlank()) {
            imagesDir = Path.of(uploads).resolve("images");
        } else {
            imagesDir = Path.of(System.getProperty("user.dir"), "uploads", "images");
        }
        String location = imagesDir.toAbsolutePath().toString();
        if (!location.endsWith("/") && !location.endsWith("\\\\")) {
            location = location + System.getProperty("file.separator");
        }
        // Servir archivos bajo /images/** leyendo desde el directorio físico indicado
        // (esto debe coincidir con las URLs construidas como APP_BASE_URL + "/images/" + nombreArchivo)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + location);
    }
}
