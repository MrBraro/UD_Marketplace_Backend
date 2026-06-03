package com.udmarketplace.auth.service;

import com.udmarketplace.transaccion.dto.ReporteExternoDto;

/**
 * Cliente HTTP hacia el report service Python (C2C-UD).
 *
 * <p>Contratos consumidos:
 * <ul>
 *   <li>POST /api/v1/reports                      — crea un reporte con radicado externo</li>
 *   <li>GET  /api/v1/reports/tracking/{radicado}  — consulta estado por radicado</li>
 * </ul>
 */
public interface PythonReportClientService {

    /**
     * Registra un reporte en el report service Python y retorna el radicado externo asignado.
     * Falla silenciosamente si el servicio no está disponible.
     *
     * @param userName   nombre completo del usuario
     * @param userEmail  correo del usuario
     * @param reportType tipo de reporte (PETICION, QUEJA, RECLAMO)
     * @param subject    asunto breve del reporte
     * @param description descripción detallada
     * @return DTO con el radicado externo y estado, o null si el servicio no responde
     */
    ReporteExternoDto crearReporte(String userName, String userEmail,
                                   String reportType, String subject, String description);

    /**
     * Consulta el estado de un reporte por su radicado externo.
     *
     * @param radicado número de radicado asignado por el report service
     * @return DTO con el estado actual, o null si no se encuentra / servicio no disponible
     */
    ReporteExternoDto consultarPorRadicado(String radicado);
}
