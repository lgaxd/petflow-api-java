package br.com.petflow.petflow_api.security;

import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticatedTutor implements UserDetails {
    private final Long id;
    private final String email;
    private final String name;
    private final String password;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedTutor(Tutor tutor) {
        this.id = tutor.getId();
        this.email = tutor.getEmail();
        this.name = tutor.getName();
        this.password = tutor.getPasswordHash();
        this.role = tutor.getRole();
        this.authorities = role == UserRole.ADMIN
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_TUTOR"))
                : List.of(new SimpleGrantedAuthority("ROLE_TUTOR"));
    }

    public AuthenticatedTutor(Long id, String email, String name, String password, UserRole role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.authorities = role == UserRole.ADMIN
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_TUTOR"))
                : List.of(new SimpleGrantedAuthority("ROLE_TUTOR"));
    }

    public AuthenticatedTutor(Long id, String email, String name, UserRole role) {
        this(id, email, name, null, role);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getEmail() {
        return email;
    }
}
