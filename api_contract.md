# Contrato de la API de Autenticación y Autorización (Alineado con Diagrama ER)

Este documento define de forma precisa los contratos de comunicación (endpoints, métodos, payloads, cabeceras y códigos de respuesta HTTP) expuestos por el backend de autenticación de **UD Marketplace**, alineados al diagrama de Entidad-Relación (ER) y en español.

---

## 🔑 Resumen del Flujo de Autenticación en 2 Pasos

El proceso de inicio de sesión se realiza en dos etapas para garantizar la máxima seguridad:

```
Paso 1: POST /api/auth/login 
  Valida correo_usuario y password_usua (RF08).
  Si son correctas, genera un código 2FA de 6 dígitos enviado por email y responde: TWO_FACTOR_REQUIRED.

Paso 2: POST /api/auth/verifyTwoFactor
  Recibe el correo_usuario y el código de 6 dígitos (RF11).
  Si el código es correcto, emite el token JWT para uso en cabeceras HTTP.
```

---

## 📡 Endpoints Públicos

No requieren cabecera `Authorization`.

### 1. Iniciar Proceso de Login (Paso 1)
Valida las credenciales básicas y prepara el segundo factor.

- **Método:** `POST`
- **Ruta:** `/api/auth/login`
- **Cabeceras:** `Content-Type: application/json`
- **Cuerpo del Request (JSON):**
  ```json
  {
    "correoUsuario": "admin@udmarketplace.com",
    "passwordUsua": "Admin123!"
  }
  ```
- **Respuestas:**
  - **`200 OK` (Credenciales válidas, 2FA requerido):**
    ```json
    {
      "step": "TWO_FACTOR_REQUIRED",
      "correoUsuario": "admin@udmarketplace.com",
      "message": "Se ha enviado un código de verificación a tu email registrado"
    }
    ```
    *Nota: El código 2FA de 6 dígitos se registra temporalmente en la base de datos y se envía de forma simulada a la consola (Logs del servidor).*
  
  - **`401 Unauthorized` (Credenciales inválidas o incorrectas - RF08):**
    ```json
    {
      "status": 401,
      "message": "Credenciales inválidas",
      "timestamp": "2026-05-26T17:00:00"
    }
    ```
  
  - **`400 Bad Request` (Campos obligatorios faltantes o vacíos):**
    ```json
    {
      "status": 400,
      "message": "El correo de usuario es obligatorio",
      "timestamp": "2026-05-26T17:00:00"
    }
    ```

---

### 2. Verificar Código 2FA y Emitir JWT (Paso 2)
Valida el código de un solo uso recibido y emite el JWT firmado (RF11).

- **Método:** `POST`
- **Ruta:** `/api/auth/verifyTwoFactor`
- **Cabeceras:** `Content-Type: application/json`
- **Cuerpo del Request (JSON):**
  ```json
  {
    "correoUsuario": "admin@udmarketplace.com",
    "twoFactorCode": "403354"
  }
  ```
- **Respuestas:**
  - **`200 OK` (Código correcto, sesión iniciada):**
    ```json
    {
      "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI...",
      "correoUsuario": "admin@udmarketplace.com",
      "rolUsua": "ADMINISTRADOR",
      "tokenType": "Bearer"
    }
    ```
    *El token contiene los claims: sub (correoUsuario) y role (rolUsua: ADMINISTRADOR, VENDEDOR, COMPRADOR) con una expiración de 24 horas.*

  - **`401 Unauthorized` (Código incorrecto o expirado - RF11):**
    ```json
    {
      "status": 401,
      "message": "Código de verificación inválido",
      "timestamp": "2026-05-26T17:05:00"
    }
    ```

---

## 🔒 Endpoints Protegidos
Requieren la cabecera `Authorization: Bearer <token>` con un JWT válido emitido en el paso 2 y no invalidado por logout.

### 3. Cerrar Sesión / Logout (RF13, RF25)
Procesa la solicitud de cierre de sesión explícito agregando el token a la blacklist de base de datos.

- **Método:** `POST`
- **Ruta:** `/api/auth/logout`
- **Cabeceras:** 
  - `Authorization: Bearer <token>`
- **Cuerpo del Request:** Vacío.
- **Respuestas:**
  - **`200 OK` (Sesión cerrada exitosamente):**
    ```json
    {
      "message": "Sesión cerrada exitosamente"
    }
    ```
  - **`401 Unauthorized` (Token ausente, inválido, expirado o ya invalidado):**
    ```json
    {
      "status": 401,
      "message": "No autenticado: token ausente o inválido",
      "timestamp": "2026-05-26T17:10:00"
    }
    ```

---

### 4. Obtener Información de Usuario Autenticado
Retorna los datos del usuario autenticado en base al token JWT enviado, mapeando todos los campos requeridos en el diagrama ER.

- **Método:** `GET`
- **Ruta:** `/api/auth/me`
- **Cabeceras:** 
  - `Authorization: Bearer <token>`
- **Cuerpo del Request:** Vacío.
- **Respuestas:**
  - **`200 OK` (Retorno de información completo - ER mapeado):**
    ```json
    {
      "codigoUsua": 1,
      "correoUsuario": "admin@udmarketplace.com",
      "rolUsua": "ADMINISTRADOR",
      "primerNombre": "Carlos",
      "segundoNombre": "Augusto",
      "primerApellido": "Pérez",
      "segundoApellido": "Gómez",
      "genero": "Masculino",
      "fechaNacimiento": "1985-05-15"
    }
    ```

---

### 5. Demostración de Autorización por Rol (RF24)
Endpoints restringidos a roles específicos del negocio: `ADMINISTRADOR`, `VENDEDOR` o `COMPRADOR`.

#### A. Panel Administrativo (ADMINISTRADOR)
- **Ruta:** `/api/admin/dashboard`
- **Método:** `GET`
- **Permitido para:** `ADMINISTRADOR`

#### B. Gestión de Vendedor (VENDEDOR)
- **Ruta:** `/api/seller/products`
- **Método:** `GET`
- **Permitido para:** `VENDEDOR`

#### C. Catálogo de Comprador (COMPRADOR)
- **Ruta:** `/api/buyer/catalog`
- **Método:** `GET`
- **Permitido para:** `COMPRADOR`

- **Respuestas para Endpoints de Rol:**
  - **`200 OK` (Acceso concedido):**
    ```json
    {
      "message": "Bienvenido al panel administrativo — Acceso ADMINISTRADOR"
    }
    ```
  - **`403 Forbidden` (Acceso denegado - Rol incorrecto o permisos insuficientes - RF24):**
    ```json
    {
      "status": 403,
      "message": "Acceso denegado: permisos insuficientes",
      "timestamp": "2026-05-26T17:15:00"
    }
    ```

---

## 🧪 Usuarios de Prueba (Seed Data)
Cargados automáticamente al arrancar la aplicación para pruebas:

| CorreoUsuario | Contraseña | Rol | Nombre Completo | Género | Fecha Nacimiento |
|---|---|---|---|---|---|
| `admin@udmarketplace.com` | `Admin123!` | `ADMINISTRADOR` | Carlos Augusto Pérez Gómez | Masculino | 1985-05-15 |
| `seller1@udmarketplace.com` | `Seller123!` | `VENDEDOR` | María Isabel Rodríguez Sánchez | Femenino | 1990-08-22 |
| `buyer1@udmarketplace.com` | `Buyer123!` | `COMPRADOR` | Juan García Martínez | Masculino | 1995-12-10 |
