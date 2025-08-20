package com.example.ecommerce.ecom_backend.controllers;

import com.example.ecommerce.ecom_backend.product.dto.ProductRequestDTO;
import com.example.ecommerce.ecom_backend.product.model.Product;
import com.example.ecommerce.ecom_backend.product.repository.ProductRepository;
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest( // Add this "properties" attribute to disable Redis during the test run
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerIntegrationTest {

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0.26");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

   @Autowired
   private UserRepository userRepository;





    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    void whenCreateProduct_withAdminRole_thenStatus201() throws Exception {
        // TODO
        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setName("Test Product");
        productRequestDTO.setDescription("Test Description");
        productRequestDTO.setPrice(BigDecimal.valueOf(9.99));
        productRequestDTO.setStockQuantity(10);
        productRequestDTO.setImageUrl("https://example.com/image.jpg");

        // CORRECT - .with() is chained to the RequestBuilder inside perform()
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDTO))
                        .with(user("admin@example.com").roles("ADMIN"))) // <-- Now in the correct place
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.jpg"));


       assertThat(productRepository.count()).isGreaterThan(0);
        Product savedProduct = productRepository.findAll().get(0);
        assertThat(savedProduct.getName()).isEqualTo("Test Product");
        assertThat(savedProduct.getDescription()).isEqualTo("Test Description");
        assertThat(savedProduct.getPrice()).isEqualTo(BigDecimal.valueOf(9.99));
        assertThat(savedProduct.getStockQuantity()).isEqualTo(10);
        assertThat(savedProduct.getImageUrl()).isEqualTo("https://example.com/image.jpg");
    }



}
