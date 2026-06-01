/**
 * Pruebas unitarias para comprobar el rol de admin en las categorías.
 * Cubre seguridad de acceso para los endpoints de categorías. 
 * Verifica que solo un usuario con rol ADMINISTRADOR pueda crear e inactivar categorías, 
 * y que un usuario sin autenticación o con rol incorrecto reciba 401 o 403. (para rf28 y rf30)
 *
 * @version 1.0
 * @since 2026-06-1
 */
package com.udmarketplace.catalogo.service.catalogo.controller;

import com.udmarketplace.auth.config.SecurityConfig;
import com.udmarketplace.auth.security.JwtFilter;
import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.catalogo.dto.CategoriaDto;
import com.udmarketplace.catalogo.service.CategoriaService;
import com.udmarketplace.catalogo.controller.CategoriaController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@Import(SecurityConfig.class)
@DisplayName("CategoriaSecurityTest")
class CategoriaSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    @DisplayName("Sin autenticación retorna 401")
    void crearCategoria_sinAutenticacion_retorna401() throws Exception {
        String body = """
                {
                  "nombreCat": "Electrónica",
                  "descripcionCat": "Dispositivos electrónicos"
                }
                """;

        mockMvc.perform(post("/api/admin/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    @DisplayName("COMPRADOR retorna 403")
    void crearCategoria_conComprador_retorna403() throws Exception {
        String body = """
                {
                  "nombreCat": "Electrónica",
                  "descripcionCat": "Dispositivos electrónicos"
                }
                """;

        mockMvc.perform(post("/api/admin/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    @DisplayName("VENDEDOR retorna 403")
    void crearCategoria_conVendedor_retorna403() throws Exception {
        String body = """
                {
                  "nombreCat": "Electrónica",
                  "descripcionCat": "Dispositivos electrónicos"
                }
                """;

        mockMvc.perform(post("/api/admin/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("ADMINISTRADOR retorna 201")
    void crearCategoria_conAdmin_retorna201() throws Exception {
        CategoriaDto dto = new CategoriaDto();
        dto.setIdCategoria(1L);
        dto.setNombreCat("Electrónica");
        dto.setDescripcionCat("Dispositivos electrónicos");
        dto.setActivoCat(true);
        dto.setContadorProductos(0);

        when(jwtUtil.extractUserId("token123")).thenReturn(1L);
        when(categoriaService.crearCategoria(any(), anyLong())).thenReturn(dto);

        String body = """
                {
                  "nombreCat": "Electrónica",
                  "descripcionCat": "Dispositivos electrónicos"
                }
                """;

        mockMvc.perform(post("/api/admin/categorias")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombreCat").value("Electrónica"));
    }

    @Test
    @DisplayName("Sin autenticación retorna 401")
    void inactivarCategoria_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(patch("/api/admin/categorias/1/inactivar"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    @DisplayName("COMPRADOR retorna 403")
    void inactivarCategoria_conComprador_retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/categorias/1/inactivar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    @DisplayName("VENDEDOR retorna 403")
    void inactivarCategoria_conVendedor_retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/categorias/1/inactivar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("ADMINISTRADOR retorna 204")
    void inactivarCategoria_conAdmin_retorna204() throws Exception {
        when(jwtUtil.extractUserId("token123")).thenReturn(1L);
        doNothing().when(categoriaService).inactivarCategoria(1L, 1L);

        mockMvc.perform(patch("/api/admin/categorias/1/inactivar")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isNoContent());
    }
}