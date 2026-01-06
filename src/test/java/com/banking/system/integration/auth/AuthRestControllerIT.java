package com.banking.system.integration.auth;

import com.banking.system.integration.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRestControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "test@example.com");
        registerRequest.put("password", "SecurePass123");
        registerRequest.put("firstName", "John");
        registerRequest.put("lastName", "Doe");
        registerRequest.put("documentType", "DNI");
        registerRequest.put("documentNumber", "12345678");
        registerRequest.put("birthDate", LocalDate.of(1990, 1, 1).toString());
        registerRequest.put("phone", "+1234567890");
        registerRequest.put("address", "123 Main St");
        registerRequest.put("city", "New York");
        registerRequest.put("country", "US");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldFailRegisterWithInvalidEmail() throws Exception {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "invalid-email");
        registerRequest.put("password", "SecurePass123");
        registerRequest.put("firstName", "John");
        registerRequest.put("lastName", "Doe");
        registerRequest.put("documentType", "DNI");
        registerRequest.put("documentNumber", "12345678");
        registerRequest.put("birthDate", LocalDate.of(1990, 1, 1).toString());
        registerRequest.put("phone", "+1234567890");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailRegisterWithShortPassword() throws Exception {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "test2@example.com");
        registerRequest.put("password", "short");
        registerRequest.put("firstName", "John");
        registerRequest.put("lastName", "Doe");
        registerRequest.put("documentType", "DNI");
        registerRequest.put("documentNumber", "12345678");
        registerRequest.put("birthDate", LocalDate.of(1990, 1, 1).toString());
        registerRequest.put("phone", "+1234567890");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailRegisterWithMissingRequiredFields() throws Exception {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "test3@example.com");
        registerRequest.put("password", "SecurePass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailRegisterWithDuplicateEmail() throws Exception {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "duplicate@example.com");
        registerRequest.put("password", "SecurePass123");
        registerRequest.put("firstName", "John");
        registerRequest.put("lastName", "Doe");
        registerRequest.put("documentType", "DNI");
        registerRequest.put("documentNumber", "12345678");
        registerRequest.put("birthDate", LocalDate.of(1990, 1, 1).toString());
        registerRequest.put("phone", "+1234567890");
        registerRequest.put("address", "123 Main St");
        registerRequest.put("city", "New York");
        registerRequest.put("country", "US");

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Second registration with same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // First, register a user
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "login@example.com");
        registerRequest.put("password", "SecurePass123");
        registerRequest.put("firstName", "Jane");
        registerRequest.put("lastName", "Smith");
        registerRequest.put("documentType", "DNI");
        registerRequest.put("documentNumber", "87654321");
        registerRequest.put("birthDate", LocalDate.of(1992, 5, 15).toString());
        registerRequest.put("phone", "+0987654321");
        registerRequest.put("address", "456 Oak Ave");
        registerRequest.put("city", "Los Angeles");
        registerRequest.put("country", "US");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Then, login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "login@example.com");
        loginRequest.put("password", "SecurePass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.accessToken").value(notNullValue()));
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        // First, register a user
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "wrongpass@example.com");
        registerRequest.put("password", "CorrectPass123");
        registerRequest.put("firstName", "Bob");
        registerRequest.put("lastName", "Johnson");
        registerRequest.put("documentType", "DNI");
        registerRequest.put("documentNumber", "11223344");
        registerRequest.put("birthDate", LocalDate.of(1988, 3, 20).toString());
        registerRequest.put("phone", "+1122334455");
        registerRequest.put("address", "789 Pine Rd");
        registerRequest.put("city", "Chicago");
        registerRequest.put("country", "US");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Try to login with wrong password
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "wrongpass@example.com");
        loginRequest.put("password", "WrongPassword123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoginWithNonExistentUser() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "nonexistent@example.com");
        loginRequest.put("password", "SomePassword123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoginWithInvalidEmailFormat() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "not-an-email");
        loginRequest.put("password", "SomePassword123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailLoginWithShortPassword() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "short");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}
