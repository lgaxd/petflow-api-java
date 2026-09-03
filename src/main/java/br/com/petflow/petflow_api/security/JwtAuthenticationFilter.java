package br.com.petflow.petflow_api.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import br.com.petflow.petflow_api.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        DecodedJWT decoded = jwtService.validateToken(token);

        if (decoded != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = decoded.getClaim("role").asString();
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            if ("ADMIN".equalsIgnoreCase(role)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_TUTOR"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_TUTOR"));
            }

                AuthenticatedTutor tutor = new AuthenticatedTutor(
                    decoded.getClaim("id").asLong(),
                    decoded.getSubject(),
                    decoded.getClaim("name").asString(),
                    null,
                    UserRole.valueOf(role.toUpperCase())
                );

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(tutor, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
