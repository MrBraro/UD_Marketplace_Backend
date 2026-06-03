package com.udmarketplace.transaccion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Respuesta del report service Python para creación y consulta de reportes.
 * Mapeada desde {@code POST /api/v1/reports} y {@code GET /api/v1/reports/tracking/{radicado}}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteExternoDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("radicado")
    private String radicado;

    @JsonProperty("status")
    private String status;

    @JsonProperty("report_type")
    private String reportType;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("response_message")
    private String responseMessage;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
