package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotBlank(message = "El primer nombre es obligatorio")
    private String primerNombre;

    private String segundoNombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    private String primerApellido;

    private String segundoApellido;

    @NotBlank(message = "El lugar de nacimiento es obligatorio")
    private String lugarNacimiento;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telUser;

    @NotBlank(message = "El género es obligatorio")
    private String genero;

    @NotBlank(message = "El correo institucional es obligatorio")
    @Email(message = "El correo institucional no es válido")
    private String correoInstitu;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotBlank(message = "El código estudiantil es obligatorio")
    private String codigoEstudiantil;

    @NotBlank(message = "El estado académico es obligatorio")
    private String estadoAcademico;

    @NotBlank(message = "El proyecto curricular es obligatorio")
    private String proyectoCurricular;

    @NotBlank(message = "El rol solicitado es obligatorio")
    private String permisoUser;
}