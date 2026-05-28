package com.udmarketplace.pqr.model;

/**
 * Enumeración de los tipos de solicitud disponibles para una PQR en el sistema UD Marketplace.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public enum TipoPqr {

    /** Solicitud de información, aclaración o prestación de un servicio. */
    PETICION,

    /** Manifestación de inconformidad por una situación irregular. */
    QUEJA,

    /** Exigencia del reconocimiento o reparación de un derecho. */
    RECLAMO
}
