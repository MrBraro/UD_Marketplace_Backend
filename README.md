# UD Marketplace — Backend Java (Spring Boot)



---

## Tabla de Contenido

1. [Stack tecnológico](#1-stack-tecnológico)
2. [Estructura del proyecto](#2-estructura-del-proyecto)
3. [Configuración y arranque](#3-configuración-y-arranque)
4. [Módulos implementados](#4-módulos-implementados)
5. [Requerimientos cumplidos](#5-requerimientos-cumplidos)
6. [Contratos de API](#6-contratos-de-api)
   - [6.1 Auth](#61-módulo-auth)
   - [6.2 Catálogo — Categorías](#62-módulo-catálogo--categorías)
   - [6.3 Catálogo — Productos](#63-módulo-catálogo--productos)
   - [6.4 Transacciones](#64-módulo-transacciones)
   - [6.5 PQR](#65-módulo-pqr)
   - [6.6 Valoraciones](#66-módulo-valoraciones)
7. [Modelo de errores](#7-modelo-de-errores)
8. [Usuarios de prueba (DataSeeder)](#8-usuarios-de-prueba-dataseeder)
9. [Pendientes y correcciones necesarias](#9-pendientes-y-correcciones-necesarias)
10. [Notas para producción](#10-notas-para-producción)

---

## 1. Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.2.5 | Framework base |
| Spring Security | 6.x | Autenticación y RBAC |
| Spring Data JPA / Hibernate | 6.x | Persistencia |
| MySQL | 8.x | Base de datos |
| jjwt (io.jsonwebtoken) | 0.12.3 | Generación y validación JWT |
| Lombok | 1.18.x | Reducción de boilerplate |
| Maven | 3.x | Gestión de dependencias |
| Jakarta Validation | 3.x | Validación de DTOs |

---

## 2. Estructura del proyecto

```
src/main/java/com/udmarketplace/
│
├── auth/                          # Módulo de autenticación y seguridad
│   ├── config/                    # SecurityConfig, RestTemplateConfig, DataSeeder
│   ├── controller/                # AuthController, ProtectedController
│   ├── dto/                       # Login/Register request+response, ErrorResponse
│   ├── exception/                 # Excepciones tipificadas + GlobalExceptionHandler
│   ├── mapper/                    # UserMapper
│   ├── model/                     # User, Administrador, Vendedor, Comprador, Role, ...
│   ├── repository/                # UserRepository, IntentoFallidoAuthRepository, ...
│   ├── security/                  # JwtUtil, JwtFilter, CustomUserDetailsService
│   └── service/                   # AuthService, TwoFactorService, RecuperacionPasswordService, ...
│
├── catalogo/                      # Módulo de categorías y productos
│   ├── controller/                # CategoriaController, ProductoController
│   ├── dto/                       # CategoriaDto, ProductoDto, CrearProductoRequest, ...
│   ├── model/                     # Categoria, Producto
│   ├── repository/                # CategoriaRepository, ProductoRepository
│   └── service/                   # CategoriaService, ProductoService (+ impls)
│
├── transaccion/                   # Módulo de transacciones y órdenes
│   ├── controller/                # TransaccionController
│   ├── dto/                       # TransaccionDto, OrdenEntregaDto, ...
│   ├── model/                     # Orden, DetalleOrdenEntrega, EstadoOrden
│   ├── repository/                # OrdenRepository, DetalleOrdenEntregaRepository
│   └── service/                   # TransaccionService (+ impl)
│
├── pqr/                           # Módulo de Peticiones, Quejas y Reclamos
│   ├── controller/                # PqrController
│   ├── dto/                       # PqrDto, InteraccionDto, CrearPqrRequest, ...
│   ├── model/                     # Pqr, InteraccionPqr, TipoPqr, EstadoPqr
│   ├── repository/                # PqrRepository, InteraccionPqrRepository
│   └── service/                   # PqrService (+ impl)
│
└── valoracion/                    # Módulo de valoraciones y reputación
    ├── controller/                # ValoracionController
    ├── dto/                       # ValoracionDto, ReputacionVendedorDto, ...
    ├── model/                     # Valoracion, ResenaPredefinida
    ├── repository/                # ValoracionRepository, ResenaPredefinidaRepository
    └── service/                   # ValoracionService (+ impl)
```

---

## 3. Configuración y arranque

### `application.properties` — Variables clave

```properties
# Puerto
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/marketplace?useSSL=false&serverTimezone=America/Bogota
spring.datasource.username=root
spring.datasource.password=MySQL
spring.jpa.hibernate.ddl-auto=none   # La BD ya existe; JPA solo mapea, no crea tablas

# JWT
app.jwt.secret=UdMarketplace_SuperSecretKey_2024_MustBe32CharsOrMore!
app.jwt.expiration-ms=86400000       # 24 horas

# Seguridad
app.auth.max-intentos-fallidos=5     # Intentos antes de bloqueo
app.auth.minutos-bloqueo=30          # Duración del bloqueo temporal
app.auth.minutos-expiry-2fa=10       # Vigencia del código 2FA
app.auth.minutos-expiry-token-recuperacion=60

# Backend Python (emails / geolocalización)
app.python.base-url=http://localhost:5000   # ← Cambiar al URL real

# Archivos adjuntos
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```

### Arranque

```bash
# Requisito: MySQL corriendo con la BD "marketplace" creada
mvn spring-boot:run
# Servidor disponible en http://localhost:8080
```

---

## 4. Módulos implementados

### Auth
- Login en dos pasos (credenciales → código 2FA por email)
- Emisión de JWT con `userId`, `role` y `sub` (correo)
- Logout con lista negra de tokens en base de datos
- Registro de intentos fallidos con IP, fecha y hora
- Bloqueo temporal de cuenta (configurable: 5 intentos / 30 min)
- Perfil del usuario autenticado (`GET /me`)
- Recuperación de contraseña con token de un solo uso (60 min)
- Cambio de contraseña con validación del token de recuperación
- RBAC completo vía `@PreAuthorize` por rol: `ADMINISTRADOR`, `VENDEDOR`, `COMPRADOR`
- Integración con backend Python para envío de emails (con fallback stub)

### Catálogo
- CRUD de categorías (solo ADMIN puede crear/inactivar)
- Contador automático de productos activos por categoría (actualizado en cada alta/baja)
- CRUD de productos con imagen como BLOB en BD
- Búsqueda y filtrado de productos por: nombre, categoría, precio (rango), calificación mínima, ubicación, disponibilidad
- Ordenamiento configurable: `PRECIO_ASC`, `PRECIO_DESC`, `FECHA_DESC`, `NOMBRE_ASC`
- Eliminación lógica de productos (soft-delete, flag `activo_pub`)
- Calificación promedio incluida en `ProductoDto`

### Transacciones
- Registro de intención de compra (comprador + producto → orden PENDIENTE)
- Confirmación por vendedor (PENDIENTE → CONFIRMADA)
- Generación automática de orden de entrega con snapshot del producto al confirmar
- Código de confirmación único generado automáticamente (formato `UD-{id}-{uuid8}`)
- Historial de transacciones con filtros por comprador, vendedor, estado y rango de fechas
- Consulta de detalle de orden de entrega

### PQR (Peticiones, Quejas y Reclamos)
- Creación de PQR con radicado único (AUTO_INCREMENT), fecha y hora automáticas
- Tipos permitidos: `PETICION`, `QUEJA`, `RECLAMO`
- Adjunto de archivo opcional (imagen/PDF, máx. 5 MB, almacenado como BLOB)
- Asignación automática al administrador con menor carga de PQRs abiertas
- Estados: `ENVIADA` → `EN_PROCESO` → `CERRADA`
- Registro de interacciones (mensajes con autor, contenido, fecha y hora)
- Restricción de acceso: solo el creador o el administrador asignado puede ver la PQR
- No se aceptan interacciones en PQRs cerradas

### Valoraciones
- Registro de valoración con validación de compra confirmada previa
- Calificación entera del 1 al 5
- Reseñas predefinidas opcionales seleccionables por el comprador
- Preservación del historial: valoración anterior se inactiva antes de crear una nueva (no se sobrescribe)
- Relación comprador-vendedor registrada explícitamente en cada valoración
- Cálculo de calificación promedio por producto
- Cálculo de reputación del vendedor (promedio de valoraciones activas)
- Conteo de reseñas positivas (calificación ≥ 4)
- Inactivación lógica de valoraciones por ADMIN (historial conservado)
- Actualización automática de la calificación del vendedor tras cada cambio

---

## 5. Requerimientos cumplidos

| REQ | Descripción | Estado |
|-----|-------------|--------|
| REQ-12 | JWT contiene `userId` del usuario autenticado | ✅ |
| REQ-15 | Registro de intentos fallidos con IP, fecha y hora | ✅ |
| REQ-16 | Bloqueo temporal tras 5 intentos fallidos (30 min) | ✅ |
| REQ-29 | Contador de productos activos por categoría | ✅ |
| REQ-38 | Transacción asociada a comprador, vendedor y producto | ✅ |
| REQ-39 | Actualización de estado al confirmar el vendedor | ✅ |
| REQ-40 | Historial de transacciones consultable con filtros | ✅ |
| REQ-41 | Generación automática de orden de entrega al confirmar | ✅ |
| REQ-42 | Snapshot del producto en la orden de entrega | ✅ |
| REQ-44 Radicado único por PQR (AUTO_INCREMENT) | ✅ |
| REQ-46 Fecha y hora de creación registradas automáticamente | ✅ |
| REQ-47 | Adjunto de archivo (BLOB en BD, desarrollo) | ✅ |
| REQ-49 | Asignación al administrador con menor carga | ✅ |
| REQ-51 | Interacciones de PQR con autor, mensaje, fecha y hora | ✅ |
| REQ-52 | Calificación promedio del producto | ✅ |
| REQ-65 | Reputación del vendedor como promedio de valoraciones | ✅ |
| REQ-66 | Historial de valoraciones sin sobrescribir | ✅ |
| REQ-67 | Relación comprador-vendedor en cada valoración | ✅ |
| REQ-69 | Conteo de reseñas positivas (calificación ≥ 4) | ✅ |
| REQ-69 | Índices en campos de búsqueda/filtrado | ✅ (en marketplace.sql) |

---

## 6. Contratos de API

> **Base URL:** `http://localhost:8080`  
> **Autenticación:** `Authorization: Bearer <token>` en todos los endpoints protegidos  
> **Content-Type por defecto:** `application/json`

---

### 6.1 Módulo Auth

#### POST `/api/auth/login` — Paso 1: verificar credenciales

**Acceso:** Público

**Request:**
```json
{
  "correoUsuario": "admin@ud.edu.co",
  "passwordUsua": "password123"
}
```

**Response 200 — credenciales válidas (envía código 2FA al email):**
```json
{
  "step": "TWO_FACTOR_REQUIRED",
  "correoUsuario": "admin@ud.edu.co",
  "message": "Código de verificación enviado al correo registrado"
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | Credenciales válidas, código 2FA enviado |
| 401 | Credenciales incorrectas |
| 423 | Cuenta bloqueada temporalmente |

---

#### POST `/api/auth/verifyTwoFactor` — Paso 2: verificar código 2FA

**Acceso:** Público

**Request:**
```json
{
  "correoUsuario": "admin@ud.edu.co",
  "twoFactorCode": "482916"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "correoUsuario": "admin@ud.edu.co",
  "rolUsua": "ADMINISTRADOR",
  "tokenType": "Bearer"
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | Código válido, JWT emitido |
| 401 | Código inválido o expirado (10 min) |

---

#### POST `/api/auth/logout` — Cerrar sesión

**Acceso:** Autenticado

**Headers:** `Authorization: Bearer <token>`

**Response 200:**
```json
{
  "message": "Sesión cerrada correctamente"
}
```

---

#### GET `/api/auth/me` — Perfil del usuario autenticado

**Acceso:** Autenticado

**Headers:** `Authorization: Bearer <token>`

**Response 200:**
```json
{
  "codigoUsua": 1,
  "correoUsuario": "admin@ud.edu.co",
  "rolUsua": "ADMINISTRADOR",
  "primerNombre": "Admin",
  "segundoNombre": null,
  "primerApellido": "UD",
  "segundoApellido": null,
  "genero": "M",
  "fechaNacimiento": "1990-01-01"
}
```

---

#### POST `/api/auth/recover-password` — Solicitar recuperación de contraseña

**Acceso:** Público

**Request:**
```json
{
  "correoUsuario": "usuario@ud.edu.co"
}
```

**Response 200** (siempre 200 para no revelar si el correo existe):
```json
{
  "message": "Si el correo existe, recibirás instrucciones de recuperación"
}
```

---

#### POST `/api/auth/reset-password` — Cambiar contraseña con token

**Acceso:** Público

**Request:**
```json
{
  "token": "uuid-de-recuperacion",
  "nuevaPassword": "NuevaPass123!"
}
```

**Response 200:**
```json
{
  "message": "Contraseña actualizada correctamente"
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | Contraseña actualizada |
| 400 | Token inválido o expirado (60 min) |

---

### 6.2 Módulo Catálogo — Categorías

#### POST `/api/admin/categorias` — Crear categoría

**Acceso:** `ADMINISTRADOR`

**Request:**
```json
{
  "nombreCat": "Electrónica",
  "descripcionCat": "Dispositivos electrónicos y accesorios"
}
```

**Response 201:**
```json
{
  "idCategoria": 1,
  "nombreCat": "Electrónica",
  "activoCat": true,
  "descripcionCat": "Dispositivos electrónicos y accesorios",
  "contadorProductos": 0
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 201 | Categoría creada |
| 400 | Nombre de categoría ya existe |
| 403 | No es ADMINISTRADOR |

---

#### GET `/api/categorias` — Listar categorías activas

**Acceso:** Público

**Response 200:**
```json
[
  {
    "idCategoria": 1,
    "nombreCat": "Electrónica",
    "activoCat": true,
    "descripcionCat": "Dispositivos electrónicos y accesorios",
    "contadorProductos": 5
  }
]
```

---

#### GET `/api/admin/categorias` — Listar todas las categorías (incluye inactivas)

**Acceso:** `ADMINISTRADOR`

**Response 200:** `List<CategoriaDto>` (misma estructura, incluye `activoCat: false`)

---

#### PATCH `/api/admin/categorias/{id}/inactivar` — Desactivar categoría

**Acceso:** `ADMINISTRADOR`

**Response 204:** Sin cuerpo

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 204 | Categoría inactivada |
| 404 | Categoría no encontrada |
| 403 | No es ADMINISTRADOR |

---

### 6.3 Módulo Catálogo — Productos

#### POST `/api/seller/productos` — Registrar producto

**Acceso:** `VENDEDOR`  
**Content-Type:** `multipart/form-data`

**Partes:**
- `datos` (JSON): `CrearProductoRequest`
- `imagen` (file, opcional): imagen del producto (máx. 5 MB)

**`datos` JSON:**
```json
{
  "nombrePub": "Laptop Dell XPS",
  "descripcionPub": "Laptop en excelente estado, 16GB RAM",
  "precioPub": 2500000.00,
  "disponibilidad": true,
  "idCategoria": 1,
  "condicionesVenta": "Venta en mano, no envíos",
  "ubicacion": "Bogotá D.C."
}
```

**Response 201:** `ProductoDto` (ver abajo)

---

#### GET `/api/productos` — Buscar y filtrar productos

**Acceso:** Público

**Query params (todos opcionales):**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `nombre` | String | Búsqueda parcial por nombre |
| `idCategoria` | Long | Filtrar por categoría |
| `precioMin` | Double | Precio mínimo |
| `precioMax` | Double | Precio máximo |
| `disponibilidad` | Boolean | Solo disponibles |
| `ubicacion` | String | Filtrar por ubicación |
| `calificacionMin` | Double | Calificación mínima |
| `ordenarPor` | String | `PRECIO_ASC`, `PRECIO_DESC`, `FECHA_DESC`, `NOMBRE_ASC` |

**Response 200:**
```json
[
  {
    "idPub": 1,
    "idVendedor": 2,
    "nombreVendedor": "Juan Pérez",
    "nombrePub": "Laptop Dell XPS",
    "descripcionPub": "Laptop en excelente estado, 16GB RAM",
    "imagenBase64": "base64...",
    "ubicacion": "Bogotá D.C.",
    "precioPub": 2500000.00,
    "disponibilidad": true,
    "idCategoria": 1,
    "nombreCategoria": "Electrónica",
    "condicionesVenta": "Venta en mano, no envíos",
    "fechaRegistro": "2026-05-28",
    "activoPub": true,
    "calificacionPromedio": 4.5
  }
]
```

---

#### GET `/api/productos/{id}` — Detalle de producto

**Acceso:** Público

**Response 200:** `ProductoDto` (misma estructura)

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | Producto encontrado |
| 404 | Producto no existe o está inactivo |

---

#### GET `/api/seller/productos` — Listar productos del vendedor

**Acceso:** `VENDEDOR`

**Response 200:** `List<ProductoDto>` (solo los del vendedor autenticado)

---

#### PUT `/api/seller/productos/{id}` — Actualizar producto

**Acceso:** `VENDEDOR` (solo el propietario)  
**Content-Type:** `multipart/form-data`

**Partes:** Mismas que en creación (`datos` + `imagen` opcional)

**Response 200:** `ProductoDto` actualizado

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | Producto actualizado |
| 403 | No es el propietario del producto |
| 404 | Producto no existe |

---

#### DELETE `/api/seller/productos/{id}` — Eliminar producto (lógico)

**Acceso:** `VENDEDOR` (solo el propietario)

**Response 204:** Sin cuerpo. El producto queda con `activoPub = false`.

---

### 6.4 Módulo Transacciones

#### POST `/api/buyer/transacciones` — Registrar intención de compra

**Acceso:** `COMPRADOR`

**Request:**
```json
{
  "idPub": 1
}
```

**Response 201:**
```json
{
  "idOrden": 10,
  "idProducto": 1,
  "nombreProducto": "Laptop Dell XPS",
  "precioCompra": 2500000.00,
  "idComprador": 3,
  "nombreComprador": "María García",
  "idVendedor": 2,
  "nombreVendedor": "Juan Pérez",
  "estadoOrden": "PENDIENTE",
  "fechaOrden": "2026-05-28",
  "codigoConfirmacion": null,
  "detalleOrdenEntrega": null
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 201 | Transacción creada |
| 404 | Producto no existe o inactivo |
| 422 | Producto no disponible |

---

#### PATCH `/api/seller/transacciones/{id}/confirmar` — Confirmar transacción

**Acceso:** `VENDEDOR` (solo el vendedor de la orden)

**Response 200:**
```json
{
  "idOrden": 10,
  "idProducto": 1,
  "nombreProducto": "Laptop Dell XPS",
  "precioCompra": 2500000.00,
  "idComprador": 3,
  "nombreComprador": "María García",
  "idVendedor": 2,
  "nombreVendedor": "Juan Pérez",
  "estadoOrden": "CONFIRMADA",
  "fechaOrden": "2026-05-28",
  "codigoConfirmacion": "UD-10-A3F2B1C9",
  "detalleOrdenEntrega": {
    "idDetalle": 5,
    "idOrden": 10,
    "nombreProductoSnapshot": "Laptop Dell XPS",
    "descripcionSnapshot": "Laptop en excelente estado, 16GB RAM",
    "precioSnapshot": 2500000.00,
    "condicionesVentaSnapshot": "Venta en mano, no envíos",
    "ubicacionSnapshot": "Bogotá D.C.",
    "codigoConfirmacion": "UD-10-A3F2B1C9",
    "fechaConfirmacion": "2026-05-28"
  }
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | Transacción confirmada |
| 403 | No es el vendedor de la orden |
| 404 | Orden no encontrada |
| 422 | La orden no está en estado PENDIENTE |

---

#### GET `/api/transacciones/historial` — Historial de transacciones

**Acceso:** Autenticado

**Query params (todos opcionales):**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `idComprador` | Long | Filtrar por comprador |
| `idVendedor` | Long | Filtrar por vendedor |
| `estadoOrden` | String | `PENDIENTE`, `CONFIRMADA`, `CANCELADA`, `ENTREGADA` |
| `fechaDesde` | LocalDate (`yyyy-MM-dd`) | Fecha inicial |
| `fechaHasta` | LocalDate (`yyyy-MM-dd`) | Fecha final |

**Response 200:** `List<TransaccionDto>`

---

#### GET `/api/transacciones/{id}/orden` — Detalle de orden de entrega

**Acceso:** Autenticado

**Response 200:** `OrdenEntregaDto`

```json
{
  "idDetalle": 5,
  "idOrden": 10,
  "nombreProductoSnapshot": "Laptop Dell XPS",
  "descripcionSnapshot": "Laptop en excelente estado, 16GB RAM",
  "precioSnapshot": 2500000.00,
  "condicionesVentaSnapshot": "Venta en mano, no envíos",
  "ubicacionSnapshot": "Bogotá D.C.",
  "codigoConfirmacion": "UD-10-A3F2B1C9",
  "fechaConfirmacion": "2026-05-28"
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | Detalle encontrado |
| 404 | Orden no tiene detalle (aún no confirmada) |

---

### 6.5 Módulo PQR

#### POST `/api/pqrs` — Crear PQR

**Acceso:** Autenticado  
**Content-Type:** `multipart/form-data`

**Partes:**
- `datos` (JSON, obligatorio): `CrearPqrRequest`
- `adjunto` (file, opcional): imagen o PDF, máx. 5 MB

**`datos` JSON:**
```json
{
  "tipoPqr": "QUEJA",
  "descripcionPqr": "El vendedor no entregó el producto en el tiempo acordado"
}
```

> **Tipos válidos:** `PETICION`, `QUEJA`, `RECLAMO`

**Response 201:**
```json
{
  "radicado": 1001,
  "codigoUsuario": 3,
  "nombreUsuario": "María García",
  "tipoPqr": "QUEJA",
  "descripcionPqr": "El vendedor no entregó el producto en el tiempo acordado",
  "estadoPqr": "ENVIADA",
  "fechaCreacionPqr": "2026-05-28",
  "horaCreacionPqr": "14:30:00",
  "codigoAdmin": 1,
  "interacciones": []
}
```

---

#### GET `/api/pqrs` — Listar PQRs del usuario autenticado

**Acceso:** Autenticado

**Response 200:** `List<PqrDto>`

---

#### GET `/api/pqrs/{radicado}` — Detalle de PQR

**Acceso:** Autenticado (solo creador o administrador asignado)

**Response 200:** `PqrDto` completo con historial de interacciones:

```json
{
  "radicado": 1001,
  "codigoUsuario": 3,
  "nombreUsuario": "María García",
  "tipoPqr": "QUEJA",
  "descripcionPqr": "El vendedor no entregó el producto en el tiempo acordado",
  "estadoPqr": "EN_PROCESO",
  "fechaCreacionPqr": "2026-05-28",
  "horaCreacionPqr": "14:30:00",
  "codigoAdmin": 1,
  "interacciones": [
    {
      "idInteraccion": 1,
      "codigoAutor": 3,
      "nombreAutor": "María García",
      "mensaje": "Necesito que revisen mi caso urgente",
      "fechaHora": "2026-05-28T14:35:00"
    },
    {
      "idInteraccion": 2,
      "codigoAutor": 1,
      "nombreAutor": "Admin UD",
      "mensaje": "Hemos recibido su caso y lo estamos revisando",
      "fechaHora": "2026-05-28T15:00:00"
    }
  ]
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | PQR encontrada |
| 403 | No es el creador ni el admin asignado |
| 404 | Radicado no existe |

---

#### POST `/api/pqrs/{radicado}/interacciones` — Agregar mensaje a PQR

**Acceso:** Autenticado

**Request:**
```json
{
  "mensaje": "Adjunto comprobante de pago para su revisión"
}
```

**Response 201:**
```json
{
  "idInteraccion": 3,
  "codigoAutor": 3,
  "nombreAutor": "María García",
  "mensaje": "Adjunto comprobante de pago para su revisión",
  "fechaHora": "2026-05-28T16:20:00"
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 201 | Interacción registrada |
| 422 | La PQR está en estado CERRADA |
| 404 | Radicado no existe |

---

#### PATCH `/api/admin/pqrs/{radicado}/estado` — Cambiar estado de PQR

**Acceso:** `ADMINISTRADOR`

**Query param:** `estado=EN_PROCESO` *(o `ENVIADA` o `CERRADA`)*

**Response 200:** `PqrDto` con estado actualizado

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 200 | Estado actualizado |
| 400 | Estado inválido |
| 404 | Radicado no existe |

---

#### PATCH `/api/admin/pqrs/{radicado}/cerrar` — Cerrar PQR

**Acceso:** `ADMINISTRADOR`

**Response 200:** `PqrDto` con `estadoPqr: "CERRADA"`

---

### 6.6 Módulo Valoraciones

#### POST `/api/buyer/valoraciones` — Registrar valoración

**Acceso:** `COMPRADOR`

**Request:**
```json
{
  "idPub": 1,
  "idOrden": 10,
  "calificacion": 5,
  "idResena": 2
}
```

> `idResena` es opcional. `calificacion` debe ser entero entre 1 y 5.  
> La orden debe estar en estado `CONFIRMADA` y pertenecer al comprador.

**Response 201:**
```json
{
  "idVal": 1,
  "idProducto": 1,
  "nombreProducto": "Laptop Dell XPS",
  "idVendedor": 2,
  "nombreVendedor": "Juan Pérez",
  "idComprador": 3,
  "nombreComprador": "María García",
  "calificacion": 5,
  "resenaPredefinida": "Excelente vendedor, muy puntual",
  "fechaValo": "2026-05-28",
  "estadoValo": true
}
```

**Códigos de respuesta:**

| Código | Situación |
|--------|-----------|
| 201 | Valoración registrada |
| 422 | Orden no confirmada o no pertenece al comprador |
| 404 | Producto, orden o comprador no encontrado |

> Si el comprador ya tenía una valoración activa para ese producto, se inactiva automáticamente (historial preservado) y se crea la nueva.

---

#### GET `/api/valoraciones/producto/{id}` — Listar valoraciones de un producto

**Acceso:** Público

**Response 200:** `List<ValoracionDto>`

---

#### GET `/api/valoraciones/producto/{id}/promedio` — Calificación promedio del producto

**Acceso:** Público

**Response 200:** `4.7` *(Double, null si no hay valoraciones)*

---

#### GET `/api/valoraciones/vendedor/{id}/reputacion` — Reputación del vendedor

**Acceso:** Público

**Response 200:**
```json
{
  "idVendedor": 2,
  "nombreVendedor": "Juan Pérez",
  "calificacionPromedio": 4.67,
  "totalResenasPositivas": 12,
  "totalValoraciones": 15
}
```

> `totalResenasPositivas` = valoraciones activas con calificación ≥ 4.

---

#### GET `/api/valoraciones/resenas` — Catálogo de reseñas predefinidas

**Acceso:** Público

**Response 200:**
```json
[
  { "idResena": 1, "textoResena": "Producto en perfectas condiciones", "activo": true },
  { "idResena": 2, "textoResena": "Excelente vendedor, muy puntual", "activo": true },
  { "idResena": 3, "textoResena": "Producto tal como se describe", "activo": true }
]
```

---

#### PATCH `/api/admin/valoraciones/{id}/inactivar` — Inactivar valoración

**Acceso:** `ADMINISTRADOR`

**Response 204:** Sin cuerpo. La valoración queda con `estadoValo = false` y la reputación del vendedor se recalcula automáticamente.

---

## 7. Modelo de errores

Todos los errores siguen este formato estándar:

```json
{
  "status": 404,
  "message": "Producto no encontrado con id: 99",
  "timestamp": "2026-05-28T14:30:00.000Z"
}
```

**Mapa de códigos HTTP:**

| Código | Excepción | Situación |
|--------|-----------|-----------|
| 400 | `MethodArgumentNotValidException` | Validación de campos fallida |
| 401 | `InvalidCredentialsException` | Credenciales incorrectas |
| 401 | `TwoFactorException` | Código 2FA inválido o expirado |
| 401 | `InvalidTokenException` | Token JWT inválido, expirado o en blacklist |
| 403 | `AccessDeniedException` | Rol insuficiente para la operación |
| 404 | `RecursoNoEncontradoException` | Recurso no encontrado |
| 422 | `OperacionNoPermitidaException` | Operación no permitida (regla de negocio) |
| 423 | `AccountBlockedException` | Cuenta bloqueada temporalmente |
| 500 | `Exception` | Error interno no controlado |

---

## 8. Usuarios de prueba (DataSeeder)

Al arrancar, `DataSeeder` crea automáticamente tres usuarios si no existen:

| Rol | Correo | Contraseña |
|-----|--------|------------|
| ADMINISTRADOR | `admin@ud.edu.co` | `admin123` |
| VENDEDOR | `vendedor@ud.edu.co` | `vendedor123` |
| COMPRADOR | `comprador@ud.edu.co` | `comprador123` |

> **⚠️ Estos usuarios son solo para desarrollo.** 

---

