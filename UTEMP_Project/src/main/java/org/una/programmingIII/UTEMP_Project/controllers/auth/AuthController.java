package org.una.programmingIII.UTEMP_Project.controllers.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.responses.ApiResponse;
import org.una.programmingIII.UTEMP_Project.security.utils.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getIdentificationNumber(), authRequest.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getIdentificationNumber());
            final String jwt = jwtUtil.generateAccessToken((User) userDetails); // Asegúrate de que `UserDetails` sea el tipo correcto

            new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found");
            ApiResponse<String> response = new ApiResponse<>(jwt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials"));
        }
    }
}