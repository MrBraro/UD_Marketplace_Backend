/**
 * Entidad JPA que representa al subtipo Vendedor en el marketplace UD.
 *
 * <p>Mapea la tabla {@code vendedor} usando herencia {@code JOINED} desde {@link User}.
 * El campo {@code codigo_user} actúa simultáneamente como clave primaria y clave foránea
 * hacia la tabla {@code usuario}. Agrega el campo {@code calificacion} que refleja
 * la reputación del vendedor calculada como promedio de sus valoraciones activas (REQ-16).
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "vendedor")
@PrimaryKeyJoinColumn(name = "codigo_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Vendedor extends User {

    /** Calificación promedio del vendedor, recalculada tras cada valoración activa (REQ-16). */
    @Column(name = "calificacion", precision = 3, scale = 2)
    private BigDecimal calificacion;
}
