package com.raissac.budget_management.security.service;

import com.raissac.budget_management.exception.EmailAlreadyUsedException;
import com.raissac.budget_management.security.config.JwtUtil;
import com.raissac.budget_management.security.dto.AuthRequest;
import com.raissac.budget_management.security.dto.RegisterRequest;
import com.raissac.budget_management.security.entity.Role;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if ( userRepository.findByEmail(request.email()).isPresent() ) {
            throw new EmailAlreadyUsedException("Email already used!");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        logger.info("User with email {} created successfully", savedUser.getEmail());

        return savedUser;
    }

    public String login(AuthRequest request, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {

        logger.info("Login attempt for email: {}", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        logger.info("User {} authenticated successfully", request.email());

        return jwtUtil.generateToken(request.email());
    }
}
