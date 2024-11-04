package org.una.programmingIII.UTEMP_Project.services.user;

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
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
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
        return user.getIdentificationNumber();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getName() {
        return user.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getState() != UserState.INACTIVE && user.getState() != UserState.SUSPENDED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getState() != UserState.INACTIVE && user.getState() != UserState.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getState() != UserState.INACTIVE && user.getState() != UserState.SUSPENDED;
    }

    @Override
    public boolean isEnabled() {
        return user.getState() == UserState.ACTIVE;
    }
}