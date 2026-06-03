package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.service.PythonCouponClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PythonCouponClientServiceImpl implements PythonCouponClientService {

    private final RestTemplate restTemplate;

    @Value("${app.python.coupon-base-url:http://localhost:8001}")
    private String couponBaseUrl;

    @Override
    public void crearCuponCumpleanios(String correoUsuario) {
        String url = couponBaseUrl + "/api/v1/coupons/happy-birthday/" + encode(correoUsuario);
        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("[COUPON] Cupón de cumpleaños creado para: {}", correoUsuario);
        } catch (RestClientException e) {
            log.warn("[COUPON] Coupon service no disponible. Cupón de cumpleaños no creado para: {}", correoUsuario);
        }
    }

    @Override
    public BigDecimal aplicarCupon(String correoComprador, String codigoCupon, BigDecimal precioOriginal) {
        // Intenta primero como cupón asignado, luego como libre
        BigDecimal resultado = intentarAplicar("apply-assigned", correoComprador, codigoCupon, precioOriginal);
        if (resultado != null) return resultado;
        return intentarAplicar("apply-unassigned", correoComprador, codigoCupon, precioOriginal);
    }

    @Override
    public String obtenerCuponesUsuario(String correoUsuario) {
        String url = couponBaseUrl + "/api/v1/users/" + encode(correoUsuario) + "/coupons";
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            log.warn("[COUPON] No se pudieron obtener los cupones de: {}", correoUsuario);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────

    private BigDecimal intentarAplicar(String tipo, String correo, String codigo, BigDecimal precio) {
        String url = couponBaseUrl + "/api/v1/users/" + encode(correo)
                + "/coupons/" + codigo + "/" + tipo + "?original_price=" + precio.toPlainString();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(URI.create(url), null, Map.class);
            if (response != null && response.containsKey("final_price")) {
                double finalPrice = ((Number) response.get("final_price")).doubleValue();
                log.info("[COUPON] Cupón '{}' aplicado ({}): {} → {}", codigo, tipo, precio, finalPrice);
                return BigDecimal.valueOf(finalPrice);
            }
        } catch (RestClientException e) {
            log.debug("[COUPON] {} falló para cupón '{}': {}", tipo, codigo, e.getMessage());
        }
        return null;
    }

    private String encode(String value) {
        return value.replace("@", "%40");
    }
}
