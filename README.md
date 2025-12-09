# Proyecto Fullstack ll React

Aplicación fullstack de e‑commerce con backend Spring Boot y frontend React (Vite). Incluye documentación interactiva con Swagger.

**Tecnologías**
- Backend: `Java 17`, `Spring Boot 3.5.6`, `Spring Security` + `JWT (jjwt)`, `Spring Data JPA/Hibernate`, `springdoc-openapi`.
- Frontend: `React 18`, `Vite 5`, `react-router-dom`.
- Otros: `Maven Wrapper`.

**Instrucciones de instalación**
- Requisitos: `Java 17`, `Node.js 18+`.
- Backend: usa Maven Wrapper, no requiere instalación previa de Maven.
- Frontend: entrar a `vite` y descargar dependencias con `npm install`.

**Instrucciones de ejecución**
- Backend:
  - En la raíz: `mvnw.cmd spring-boot:run`
  - API: `http://localhost:8080`
  - Swagger UI: `http://localhost:8080/swagger-ui/index.html` (alternativa: `http://localhost:8080/swagger-ui.html`)
- Frontend:
  - `cd vite`
  - Crear `.env` con `VITE_API_BASE=http://localhost:8080`
  - Desarrollo: `npm run dev` y abrir `http://localhost:5173`
  - Producción: `npm run build` y `npm run preview`

**Credenciales de prueba**
- Admin: `admin1@youka.cl`, `admin2@youka.cl`
- Clientes: `cliente1@youka.cl` … `cliente5@youka.cl`
- Contraseña: misma para todos, almacenada como hash BCrypt en la base.
  - Si no conoces el texto plano correspondiente al hash, registra una cuenta nueva con tu propia contraseña, o actualiza la contraseña de estos usuarios generando tu hash con BCrypt y reemplazándolo.

**Documentación de API (Swagger / Postman)**
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- En Postman: importa la URL `/v3/api-docs` para generar la colección.

**Endpoints principales**
- `POST /api/autenticacion/login` — devuelve JWT
- `POST /api/autenticacion/register` — registro de usuario
- `GET /api/usuarios/me` — perfil del usuario autenticado
- `GET /api/usuarios` — listar usuarios (requiere `ADMIN`)
- `PUT /api/usuarios/{id}` — actualizar usuario (requiere `ADMIN`)
- `DELETE /api/usuarios/{id}` — deshabilitar usuario (requiere `ADMIN`)
- `PATCH /api/usuarios/{id}/estado` — cambiar habilitado (requiere `ADMIN`)
- `GET/POST /api/proveedores` — gestión de proveedores
- `GET/POST /api/carrito` — gestión del carrito
