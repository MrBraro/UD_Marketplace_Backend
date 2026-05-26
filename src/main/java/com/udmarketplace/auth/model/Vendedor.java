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

/**
 * Entidad que representa al subtipo Vendedor.
 *
 * <p>Mapeado al diccionario de datos:
 * <ul>
 *   <li>codigo_user (clave primaria y foránea conectada a usuario)</li>
 *   <li>calificacion (promedio de reputación del vendedor)</li>
 * </ul>
 */
@Entity
@Table(name = "vendedor")
@PrimaryKeyJoinColumn(name = "codigo_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Vendedor extends User {

    @Column(name = "calificacion", precision = 3, scale = 2)
    private BigDecimal calificacion;
}
