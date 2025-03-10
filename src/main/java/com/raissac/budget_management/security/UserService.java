package com.raissac.budget_management.security;

import com.raissac.budget_management.security.config.JwtUtil;
import com.raissac.budget_management.security.dto.AuthRequest;
import com.raissac.budget_management.security.dto.RegisterRequest;
import com.raissac.budget_management.security.entity.Role;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if ( userRepository.findByEmail(request.email()).isPresent() ) {
            throw new RuntimeException("Email already used!");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    public String login(AuthRequest request, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("Invalid credentials!");
        }

        return jwtUtil.generateToken(request.email());
    }
}
