package org.una.programmingIII.UTEMP_Project.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.una.programmingIII.UTEMP_Project.controllers.request.AuthRequest;
import org.una.programmingIII.UTEMP_Project.controllers.responses.ApiResponse;
import org.una.programmingIII.UTEMP_Project.controllers.responses.TokenResponse;
import org.una.programmingIII.UTEMP_Project.dtos.UserDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.UserNotFoundException;
import org.una.programmingIII.UTEMP_Project.security.utils.JwtTokenProvider;
import org.una.programmingIII.UTEMP_Project.services.CustomUserDetails;
import org.una.programmingIII.UTEMP_Project.services.CustomUserDetailsService;
import org.una.programmingIII.UTEMP_Project.services.UserService;

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

    @Operation(
            summary = "Authenticate User",
            description = "Validates user credentials and generates an access token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful. Returns JWT token and user details."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials. Authentication failed."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found.")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> createAuthenticationToken(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Autenticar al usuario
            authenticate(authRequest);
            // Obtener detalles del usuario autenticado
            CustomUserDetails userDetails = loadUserDetails(authRequest.getIdentificationNumber());
            // Generar el token JWT
            String jwt = jwtTokenProvider.generateAccessToken(userDetails.getUser());
            // Obtener los detalles del usuario asociados al ID del token
            UserDTO user = getUserById(jwtTokenProvider.getIdFromToken(jwt));

            // Construir la respuesta de token
            TokenResponse tokenResponse = buildTokenResponse(jwt, user);
            ApiResponse<TokenResponse> response = new ApiResponse<>();
            response.setData(tokenResponse);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException | UserNotFoundException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "No encontrado");
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno");
        }
    }

    // Métodos auxiliares para mejorar la legibilidad

    private void authenticate(AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getIdentificationNumber(), authRequest.getPassword())
        );
    }

    private CustomUserDetails loadUserDetails(String username) {
        return (CustomUserDetails) userDetailsService.loadUserByUsername(username);
    }

    private UserDTO getUserById(Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private TokenResponse buildTokenResponse(String jwt, UserDTO user) {
        return TokenResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .user(user)
                .build();
    }

    private ResponseEntity<ApiResponse<TokenResponse>> buildErrorResponse(HttpStatus status, String message) {
        ApiResponse<TokenResponse> errorResponse = new ApiResponse<>();
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(message);
        return ResponseEntity.status(status).body(errorResponse);
    }
}