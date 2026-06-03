package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.service.PythonReportClientService;
import com.udmarketplace.transaccion.dto.ReporteExternoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PythonReportClientServiceImpl implements PythonReportClientService {

    private final RestTemplate restTemplate;

    @Value("${app.python.report-base-url:http://localhost:8004}")
    private String reportBaseUrl;

    @Override
    public ReporteExternoDto crearReporte(String userName, String userEmail,
                                          String reportType, String subject, String description) {
        String url = reportBaseUrl + "/api/v1/reports";

        Map<String, String> payload = new HashMap<>();
        payload.put("user_name", userName);
        payload.put("user_email", userEmail);
        payload.put("report_type", reportType);
        payload.put("subject", subject);
        payload.put("description", description);

        try {
            ReporteExternoDto dto = restTemplate.postForObject(url, payload, ReporteExternoDto.class);
            if (dto != null) {
                log.info("[REPORT] Reporte creado con radicado externo: {}", dto.getRadicado());
            }
            return dto;
        } catch (RestClientException e) {
            log.warn("[REPORT] Report service no disponible. Reporte externo no creado para: {}", userEmail);
            return null;
        }
    }

    @Override
    public ReporteExternoDto consultarPorRadicado(String radicado) {
        String url = reportBaseUrl + "/api/v1/reports/tracking/" + radicado;
        try {
            return restTemplate.getForObject(url, ReporteExternoDto.class);
        } catch (RestClientException e) {
            log.warn("[REPORT] No se pudo consultar el reporte con radicado: {}", radicado);
            return null;
        }
    }
}
