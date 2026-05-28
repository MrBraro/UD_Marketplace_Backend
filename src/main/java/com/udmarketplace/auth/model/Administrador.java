package com.udmarketplace.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa al subtipo Administrador en la herencia JOINED de {@link User}.
 *
 * <p>Mapeada a la tabla {@code administrador}, vinculada a {@code usuario}
 * mediante {@code codigo_user} (clave primaria y foránea simultáneamente).
 *
 * <p>Los administradores tienen capacidades exclusivas como:
 * <ul>
 *   <li>Crear y desactivar categorías de productos</li>
 *   <li>Gestionar usuarios (actualizar datos, cambiar estado)</li>
 *   <li>Recibir y gestionar PQRs asignadas automáticamente</li>
 *   <li>Inactivar valoraciones</li>
 * </ul>
 *

 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "administrador")
@PrimaryKeyJoinColumn(name = "codigo_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Administrador extends User {

    /**
     * Número de contrato laboral o de prestación de servicios del administrador.
     * Valor único en el sistema.
     */
    @Column(name = "numero_contrato", unique = true)
    private Integer numeroContrato;
}
