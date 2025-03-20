package com.raissac.budget_management.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raissac.budget_management.exception.EmailAlreadyUsedException;
import com.raissac.budget_management.security.config.CustomUserDetailsService;
import com.raissac.budget_management.security.config.JwtUtil;
import com.raissac.budget_management.security.controller.AuthController;
import com.raissac.budget_management.security.dto.AuthRequest;
import com.raissac.budget_management.security.dto.RegisterRequest;
import com.raissac.budget_management.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200_whenRegisterIsValid() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "Ana",
                "Popescu",
                LocalDate.of(1998,8,2),
                "ana.popescu@mail.com",
                "parola123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void shouldReturn400_whenRegisterIsInvalid() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "",
                "",
                null,
                "ana.popescumail.com",
                "123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldReturn409_whenEmailAlreadyUsed() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest(
                "Ana",
                "Popescu",
                LocalDate.of(1998,8,2),
                "ana.popescu@mail.com",
                "parola123"
        );

        when(userService.register(registerRequest)).thenThrow(new EmailAlreadyUsedException("Email already used!"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already used!"));
    }

    @Test
    void shouldReturn200_whenLoginIsValid() throws Exception {
        AuthRequest request = new AuthRequest("ana.popescu@mail.com", "parola123");

        when(userService.login(eq(request), any(), any())).thenReturn("token");

        mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("token"));

    }

    @Test
    void shouldReturn401_whenLoginFails() throws Exception {
        AuthRequest request = new AuthRequest("ana.popescu@mail.com", "parolagresita");

        when(userService.login(eq(request), any(), any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}
