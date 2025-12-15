package com.rut.booking.security;

import com.rut.booking.models.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getCode().name())
        );
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getIsActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }

    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public String getShortName() {
        return user.getShortName();
    }

    public String getRoleDisplayName() {
        return user.getRole().getCode().getDisplayName();
    }

    public boolean isTeacher() {
        return "TEACHER".equals(user.getRole().getCode().name());
    }

    public boolean isDispatcher() {
        return "DISPATCHER".equals(user.getRole().getCode().name());
    }

    public boolean isAdmin() {
        return "ADMIN".equals(user.getRole().getCode().name());
    }
}
