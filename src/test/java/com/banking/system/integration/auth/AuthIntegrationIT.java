package com.banking.system.integration.auth;

import com.banking.system.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthIntegrationIT extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerUser_whenValidRequest_shouldReturnPersistedUser() throws Exception {
        String registerRequest = """
                {
                  "email": "john.doe@test.com",
                  "password": "Password123!",
                  "firstName": "John",
                  "lastName": "Doe",
                  "documentType": "DNI",
                  "documentNumber": "12345678",
                  "birthDate": "1990-01-01",
                  "phone": "+5491122334455",
                  "address": "123 Main St",
                  "city": "Buenos Aires",
                  "country": "AR"
                }
                """;

        // When / Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("john.doe@test.com"));
    }
}