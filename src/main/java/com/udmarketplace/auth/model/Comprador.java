package com.udmarketplace.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa al subtipo Comprador.
 *
 * <p>Mapeado exactamente al diccionario de datos:
 * <ul>
 *   <li>codigo_user (clave primaria y foránea conectada a usuario)</li>
 * </ul>
 */
@Entity
@Table(name = "comprador")
@PrimaryKeyJoinColumn(name = "codigo_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Comprador extends User {
}
