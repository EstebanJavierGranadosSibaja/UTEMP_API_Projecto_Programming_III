package org.una.programmingIII.UTEMP_Project.services.UserServices;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.models.UserState;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    @Getter
    private final User user;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.user = user;
        this.authorities = user.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getIdentificationNumber(); //TODO posiblemente cambiar o manejar la idea de identificacion de usuario y no usar el getUser de defecto O email, según lo que prefieras usar
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getName() {
        return user.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        // Puedes definir la lógica aquí si decides manejar expiración en el futuro
        return true; // Asumiendo que las cuentas no expiran
    }

    @Override
    public boolean isAccountNonLocked() {
        // Consideramos que una cuenta está bloqueada si está inactiva o suspendida
        return user.getState() != UserState.INACTIVE && user.getState() != UserState.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Implementa la lógica de expiración de credenciales si es necesario
        return true; // O una lógica específica
    }

    @Override
    public boolean isEnabled() {
        return user.getState() == UserState.ACTIVE; // Verifica si la cuenta está activa
    }
}