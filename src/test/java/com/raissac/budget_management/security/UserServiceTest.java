package com.raissac.budget_management.security;

import com.raissac.budget_management.exception.EmailAlreadyUsedException;
import com.raissac.budget_management.security.config.JwtUtil;
import com.raissac.budget_management.security.dto.AuthRequest;
import com.raissac.budget_management.security.dto.RegisterRequest;
import com.raissac.budget_management.security.entity.Role;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import com.raissac.budget_management.security.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setup(){
        registerRequest = new RegisterRequest(
                "Ana",
                "Popescu",
                LocalDate.of(1998,8,2),
                "ana.popescu@mail.com",
                "parola123"
        );
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists(){

        User existingUser = User.builder()
                .firstName("Ana")
                .lastName("Popescu")
                .email("ana.popescu@mail.com")
                .dateOfBirth(LocalDate.of(1998,8,2))
                .password("parola123")
                .role(Role.USER)
                .build();

        userRepository.save(existingUser);

        assertThrows(EmailAlreadyUsedException.class, () -> userService.register(registerRequest));
    }

    @Test
    void shouldSaveUser_whenRegisterRequestIsValid(){

        User savedUser = userService.register(registerRequest);

        assertNotNull(savedUser.getId());
        assertEquals(registerRequest.email(), savedUser.getEmail());
        assertEquals(Role.USER, savedUser.getRole());
        assertNotEquals(registerRequest.password(), savedUser.getPassword());
        assertTrue(passwordEncoder.matches(registerRequest.password(), savedUser.getPassword()), registerRequest.password());
    }

    @Test
    void shouldReturnToken_whenLoginIsValid(){

        AuthRequest authRequest = new AuthRequest("ana.popescu@mail.com", "parola123");

        Authentication mockAuth = Mockito.mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtUtil.generateToken(authRequest.email())).thenReturn("token");

        String token = userService.login(authRequest, authenticationManager, jwtUtil);

        assertEquals("token", token);
        verify(authenticationManager).authenticate(any());
        verify(jwtUtil).generateToken("ana.popescu@mail.com");

    }

    @Test
    void shouldThrowException_whenLoginFails(){

        AuthRequest authRequest = new AuthRequest("ana.popescu@mail.com", "parola123");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.login(authRequest, authenticationManager, jwtUtil));


        verify(authenticationManager).authenticate(any());
        verify(jwtUtil, never()).generateToken(any());


    }
}
