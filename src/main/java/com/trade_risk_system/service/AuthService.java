package com.trade_risk_system.service;

import com.trade_risk_system.dto.request.LoginRequestDTO;
import com.trade_risk_system.dto.request.RegisterRequestDTO;

import com.trade_risk_system.exception.BadRequestException;
import com.trade_risk_system.exception.DuplicateResourceException;
import com.trade_risk_system.model.User;
import com.trade_risk_system.model.enums.Role;
import com.trade_risk_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trade_risk_system.dto.response.AuthResponseDTO;
import com.trade_risk_system.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final CacheEvictionService cacheEvictionService;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            CacheEvictionService cacheEvictionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.cacheEvictionService = cacheEvictionService;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Attempting to register user: {}", request.username());
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.TRADER) // Default role
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        cacheEvictionService.evictUserDetailsCache(savedUser.getUsername());

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponseDTO(jwtToken, savedUser.getUsername(), savedUser.getRole());
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for user: {}", request.username());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (Exception e) {
            log.warn("Invalid credentials for user: {}", request.username());
            throw new BadRequestException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("User not found")); // Should not happen if auth successful

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);

        log.info("User logged in successfully: {}", user.getId());
        return new AuthResponseDTO(jwtToken, user.getUsername(), user.getRole());
    }
}
