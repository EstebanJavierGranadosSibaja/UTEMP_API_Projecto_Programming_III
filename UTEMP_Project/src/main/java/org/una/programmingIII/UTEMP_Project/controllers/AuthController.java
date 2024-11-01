package org.una.programmingIII.UTEMP_Project.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.una.programmingIII.UTEMP_Project.security.utils.JwtTokenProvider;
import org.una.programmingIII.UTEMP_Project.controllers.request.AuthRequest;
import org.una.programmingIII.UTEMP_Project.controllers.responses.ApiResponse;
import org.una.programmingIII.UTEMP_Project.controllers.responses.TokenResponse;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetails;
import org.una.programmingIII.UTEMP_Project.services.user.CustomUserDetailsService;
import org.una.programmingIII.UTEMP_Project.services.user.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/utemp/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> createAuthenticationToken(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getIdentificationNumber(), authRequest.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(authRequest.getIdentificationNumber());

            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found"));
            }

            final String jwt = jwtTokenProvider.generateAccessToken(userDetails.getUser());
            Long id = jwtTokenProvider.getIdFromToken(jwt);
            Optional<UserDTO> user = userService.getUserById(id);
            user.orElseThrow(() -> new RuntimeException("User not found"));

            TokenResponse tokenResponse = TokenResponse.builder()
                    .token(jwt)
                    .tokenType("Bearer")
                    .user(user.get())
                    .build();
            return ResponseEntity.ok(new ApiResponse<>());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials"));
        }
    }
}
