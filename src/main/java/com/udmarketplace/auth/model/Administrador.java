package com.udmarketplace.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad que representa al subtipo Administrador.
 *
 * <p>Mapeado al diccionario de datos:
 * <ul>
 *   <li>codigo_user (clave primaria y foránea conectada a usuario)</li>
 *   <li>numero_contrato (contrato laboral o de prestación de servicios)</li>
 * </ul>
 */
@Entity
@Table(name = "administrador")
@PrimaryKeyJoinColumn(name = "codigo_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Administrador extends User {

    @Column(name = "numero_contrato", length = 100)
    private String numeroContrato;
}
