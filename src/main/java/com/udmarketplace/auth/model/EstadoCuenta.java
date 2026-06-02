package com.udmarketplace.auth.model;

/**
 * Estados posibles de una cuenta de usuario en el marketplace UD.
 */
public enum EstadoCuenta {

    /** Cuenta habilitada para autenticarse y operar normalmente. */
    ACTIVA,

    /** Cuenta suspendida temporalmente por decisión administrativa. */
    SUSPENDIDA,

    /** Cuenta deshabilitada de forma definitiva o por bloqueo administrativo. */
    DESHABILITADA
}