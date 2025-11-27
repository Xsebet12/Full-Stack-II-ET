# Proyecto Fullstack ll React

Aplicación fullstack que expone una API REST de e-commerce (usuarios, autenticación JWT, catálogo, carrito, proveedores) con backend Spring Boot y frontend React (Vite). Incluye Swagger para documentación interactiva.

## Descripción del proyecto
- Backend en `Spring Boot` con `Spring Security` y `JWT` para autenticar por correo/contraseña.
- Persistencia con `JPA/Hibernate` hacia `Oracle Autonomous Database` usando Wallet (configurado en `application.properties`).
- Endpoints para autenticación, gestión de usuarios (perfil, administración), proveedores, productos y carrito.
- Frontend en `React + Vite` (SPA) consumiendo la API.
- Swagger UI para explorar y probar la API.

## Tecnologías utilizadas
- Backend:
  - `Java 21`
  - `Spring Boot 3.5.x`
  - `Spring Security`, `JWT (jjwt)`
  - `Spring Data JPA`, `Hibernate`
  - `Oracle JDBC` (Wallet), `HikariCP`
  - `Springdoc OpenAPI` (Swagger)
- Frontend:
  - `React 19`
  - `Vite 7`
  - `react-router-dom`
- Otros:
  - `Maven Wrapper`

## Instrucciones de instalación
Prerequisitos:
- `Java 21` instalado
- `Node.js 18+` (recomendado 20+)
- Acceso a la base `Oracle` (el Wallet está en `src/main/resources/Wallet_BDY110120242`)

1) Clonar o abrir el proyecto
- Ruta del proyecto backend: `..\Fullstackll`
- Frontend: `Fullstackll/vite`

2) Backend (Maven)
- No requiere instalar Maven; usa `mvnw` (wrapper) incluido

3) Frontend (Node/Vite)
- Entrar a la carpeta `vite` y descargar dependencias
  - `cd vite`
  - `npm install`

## Instrucciones de ejecución
Backend:
- En la carpeta raíz del proyecto: `./mvnw.cmd spring-boot:run`
- La API quedará disponible en `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

Frontend (opcional en desarrollo):
- `cd vite`
- `npm run dev`
- Abrir `http://localhost:5173`

Notas de configuración (backend):
- Archivo: `src/main/resources/application.properties`
  - `spring.datasource.url=jdbc:oracle:thin:@bdy110120242_high?TNS_ADMIN=src/main/resources/Wallet_BDY110120242`
  - `spring.datasource.username=...`
  - `spring.datasource.password=...`
  - Ajusta estas credenciales si cambias de base de datos.

## Credenciales de prueba
Registrar un usuario cliente (CLIENT):
- Endpoint: `POST /api/autenticacion/register`
- Body ejemplo mínimo:
```json
{
  "nombres": "Juan",
  "apellidos": "Pérez",
  "rut": "12345678",
  "dv": "9",
  "correo": "juan@example.com",
  "contrasena": "secret",
  "direccion": "Santiago"
}
```
(El backend asigna rol `CLIENT` por defecto si no se envía `rol`).

## Documentación de API (Swagger / Postman)
Swagger UI:
- `http://localhost:8080/swagger-ui.html`
- Explora endpoints: Autenticación (`/api/autenticacion/login`, `/api/autenticacion/register`), Usuarios (`/api/usuarios`, `/api/usuarios/me`), Carrito (`/api/carrito`), Proveedores (`/api/proveedores`), Productos, etc.

OpenAPI JSON:
- `http://localhost:8080/v3/api-docs`
- En Postman: importar URL del OpenAPI (`/v3/api-docs`) para generar una colección automáticamente.

### Endpoints principales
- `POST /api/autenticacion/login` — devuelve JWT
- `POST /api/autenticacion/register` — registro de usuario (por defecto CLIENT)
- `GET /api/usuarios/me` — perfil del usuario autenticado
- `GET /api/usuarios` — listar usuarios (requiere `ADMIN`)
- `PUT /api/usuarios/{id}` — actualizar usuario (requiere `ADMIN`)
- `DELETE /api/usuarios/{id}` — deshabilitar usuario (requiere `ADMIN`)
- `PATCH /api/usuarios/{id}/estado` — cambiar habilitado (requiere `ADMIN`)
- `GET/POST /api/proveedores` — gestión de proveedores (requiere autenticación)
- `GET/POST /api/carrito` — gestión del carrito (requiere autenticación)

## Troubleshooting
- Si el backend no inicia, verifica la conectividad a Oracle y el Wallet (`src/main/resources/Wallet_BDY110120242`).
- Con `spring.jpa.hibernate.ddl-auto=update` se crean/actualizan tablas en el schema configurado; asegúrate de tener permisos.
- Logs de detalle de seguridad están habilitados en `application.properties` (útil para depurar roles y JWT).