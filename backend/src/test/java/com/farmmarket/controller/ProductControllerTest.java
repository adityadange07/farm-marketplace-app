package com.farmmarket.controller;

import com.farmmarket.dto.request.CreateProductRequest;
import com.farmmarket.enums.UnitType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getProducts_Public_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getProduct_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/non-existent-slug"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "FARMER")
    void createProduct_AsFarmer_ShouldReturn201() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Test Tomatoes");
        request.setPrice(BigDecimal.valueOf(3.99));
        request.setUnit(UnitType.KG);
        request.setStockQuantity(100);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .flashAttr("product", request))
                .andExpect(status().isCreated());
    }
}