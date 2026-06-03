# UD Marketplace - Guía de Integración para Desarrollo del Frontend

Este documento detalla el flujo secuencial de llamadas a la API REST (Java Spring Boot) y la estructura de las peticiones para que la interfaz (Frontend) pueda comunicarse correctamente con el backend.

---

## 🔒 Regla General de Autenticación
Todos los endpoints protegidos por el backend requieren que se envíe la cabecera HTTP estándar de autorización con el token JWT recibido en el Login:
```http
Authorization: Bearer <TOKEN_JWT>
```

---

## 🛠️ FLUJO 1: REGISTRO DE USUARIOS (Sign Up)

El registro se realiza mediante una petición multipart debido a que los menores de edad deben adjuntar un PDF de autorización.

* **Endpoint:** `POST /api/auth/register`
* **Content-Type:** `multipart/form-data`
* **Partes de la petición (Campos en el formulario):**
  1. **`datos`** (Clave de tipo texto con valor en formato JSON):
     ```json
     {
       "tipoDocumento": "CC", // Tipo de documento (CC, TI, etc.)
       "numeroDocumento": "1002837465",
       "primerNombre": "Juan",
       "segundoNombre": "Carlos", // Opcional
       "primerApellido": "Perez",
       "segundoApellido": "Gomez", // Opcional
       "lugarNacimiento": "Bogota",
       "fechaNacimiento": "2000-01-01", // Formato YYYY-MM-DD
       "telUser": "3101234567",
       "genero": "Masculino",
       "correoInstitu": "correo@udistrital.edu.co",
       "password": "Password123!",
       "codigoEstudiantil": "20231020010",
       "estadoAcademico": "ACTIVO",
       "proyectoCurricular": "Ingenieria de Sistemas",
       "permisoUser": "COMPRADOR" // Roles válidos: COMPRADOR, VENDEDOR, ADMINISTRADOR
     }
     ```
     > **Importante:** En Postman o tu cliente HTTP, asegúrate de configurar el `Content-Type` específico de la parte `datos` como `application/json`.
  2. **`pdfAutorizacion`** (Clave de tipo archivo/file, opcional):
     * Requerido únicamente si la `fechaNacimiento` indica que el usuario es **menor de 18 años**.

* **Respuesta Exitosa (200 OK):**
  Devuelve el objeto del usuario creado sin la contraseña por seguridad.

---

## 🔑 FLUJO 2: INICIO DE SESIÓN (Login en 2 Pasos)

### Paso 1: Validar Credenciales
1. **Pantalla:** Formulario estándar de Login (correo y contraseña).
2. **Endpoint:** `POST /api/auth/login` (JSON)
3. **Cuerpo (Body):**
   ```json
   {
     "correoUsuario": "correo@udistrital.edu.co",
     "passwordUsua": "Password123!"
   }
   ```
4. **Respuesta Exitosa (200 OK):**
   ```json
   {
     "step": "TWO_FACTOR_REQUIRED",
     "correoUsuario": "correo@udistrital.edu.co",
     "message": "Se ha enviado un código de verificación a tu email registrado"
   }
   ```
5. **Acción en Frontend:** Al recibir esta respuesta, redirigir al usuario o mostrar la pantalla del Paso 2 (ingreso de código temporal).

---

### Paso 2: Verificación de Código 2FA
1. **Pantalla:** Formulario de 6 dígitos para ingresar el código OTP recibido en el correo.
2. **Endpoint:** `POST /api/auth/verifyTwoFactor` (JSON)
3. **Cuerpo (Body):**
   ```json
   {
     "correoUsuario": "correo@udistrital.edu.co",
     "twoFactorCode": "123456" // Código de 6 dígitos
   }
   ```
4. **Respuesta Exitosa (200 OK):**
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...", // Token de sesión JWT
     "correoUsuario": "correo@udistrital.edu.co",
     "rolUsua": "COMPRADOR", // Rol del usuario (COMPRADOR, VENDEDOR, ADMINISTRADOR)
     "tokenType": "Bearer"
   }
   ```
5. **Acción en Frontend:** Almacenar el campo `token` localmente (`localStorage.setItem('token', token)`) para utilizarlo en las cabeceras de las siguientes peticiones.

---

## 👤 FLUJO 3: PERFIL DE USUARIO (Obtener Datos Propios)
Consulta los detalles del usuario autenticado actual.
* **Endpoint:** `GET /api/auth/me` (Requiere Token JWT)
* **Respuesta Exitosa (200 OK):**
  ```json
  {
      "codigoUsua": 4,
      "correoUsuario": "correo@udistrital.edu.co",
      "rolUsua": "COMPRADOR",
      "primerNombre": "Juan",
      "segundoNombre": "Carlos",
      "primerApellido": "Perez",
      "segundoApellido": "Gomez",
      "genero": "Masculino",
      "fechaNacimiento": "2000-01-01"
  }
  ```

---

## 🚪 FLUJO 4: CERRAR SESIÓN (Logout)
Invalida el token JWT activo en el servidor.
* **Endpoint:** `POST /api/auth/logout` (Requiere Token JWT)
* **Acción en Frontend:** Eliminar el token del almacenamiento local y redirigir a la pantalla de inicio de sesión.

---

## 📨 FLUJO 5: RECUPERACIÓN DE CONTRASEÑA (Opcional)

### Paso 5.1: Solicitar Enlace de Recuperación
* **Endpoint:** `POST /api/auth/recuperar-password` (JSON)
* **Cuerpo (Body):**
  ```json
  {
    "correoUsuario": "correo@udistrital.edu.co"
  }
  ```
  *(El backend enviará un correo con un token único de recuperación)*.

### Paso 5.2: Restablecer Contraseña
* **Endpoint:** `POST /api/auth/reset-password` (JSON)
* **Cuerpo (Body):**
  ```json
  {
    "token": "token-uuid-recibido",
    "nuevaPassword": "NuevaPassword123!"
  }
  ```

---

## 🛒 FLUJO 6: NAVEGACIÓN Y COMPRAS (Operaciones de Negocio)

### 1. Listar Categorías (Público)
* **Endpoint:** `GET /api/categorias`
* **Respuesta (200 OK):** Lista de categorías activas (id, nombre, descripción).

### 2. Listar Productos (Público)
* **Endpoint:** `GET /api/productos`
* **Query Params (Filtros opcionales):**
  * `nombre` (búsqueda parcial)
  * `idCategoria` (filtrar por categoría)
  * `precioMin` / `precioMax`
  * `ordenarPor` (`PRECIO_ASC`, `PRECIO_DESC`, `NOMBRE_ASC`)
* **Respuesta (200 OK):** Array de productos activos en el catálogo.

### 3. Publicar Producto (Rol: VENDEDOR)
* **Endpoint:** `POST /api/seller/productos`
* **Content-Type:** `multipart/form-data`
* **Campos:**
  * `datos` (JSON con detalles del producto):
    ```json
    {
      "nombrePub": "Laptop Dell XPS",
      "descripcionPub": "16GB RAM, 512GB SSD",
      "precioPub": 2500000.00,
      "disponibilidad": true,
      "idCategoria": 1,
      "condicionesVenta": "Pago contra entrega",
      "ubicacion": "Bogota"
    }
    ```
  * `imagen` (archivo de imagen opcional).

### 4. Iniciar Intención de Compra (Rol: COMPRADOR)
* **Endpoint:** `POST /api/buyer/transacciones` (JSON)
* **Cuerpo (Body):**
  ```json
  {
    "idPub": 1 // ID del producto
  }
  ```
* **Respuesta (201 Created):** Crea la orden de compra en estado `PENDIENTE`.

### 5. Confirmar Venta (Rol: VENDEDOR)
* **Endpoint:** `PATCH /api/seller/transacciones/{idOrden}/confirmar`
* **Efecto:** Cambia el estado de la orden a `CONFIRMADA` y genera un código de confirmación digital único (ej. `UD-10-A3F2B1C9`) para que el comprador retire el producto.
