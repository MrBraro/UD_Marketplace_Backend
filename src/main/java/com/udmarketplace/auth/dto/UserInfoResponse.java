package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

/**
 * Response del endpoint GET /api/auth/me.
 *
 * <p>Retorna la información completa del usuario según los atributos del diagrama ER.
 */
@Data
@AllArgsConstructor
public class UserInfoResponse {

    private Long codigoUsua;
    private String correoUsuario;
    private String rolUsua;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String genero;
    private LocalDate fechaNacimiento;
}
