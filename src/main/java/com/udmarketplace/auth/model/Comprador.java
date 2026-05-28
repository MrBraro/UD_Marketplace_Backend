/**
 * Entidad JPA que representa al subtipo Comprador en el marketplace UD.
 *
 * <p>Mapea la tabla {@code comprador} usando herencia {@code JOINED} desde {@link User}.
 * El campo {@code codigo_user} actúa simultáneamente como clave primaria y clave foránea
 * hacia la tabla {@code usuario}. Los compradores pueden registrar valoraciones sobre
 * productos comprados (REQ-17, REQ-18).
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comprador")
@PrimaryKeyJoinColumn(name = "codigo_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Comprador extends User {
}
