package ru.istok.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.istok.backend.user.entity.UserStatus;
import ru.istok.backend.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            authenticate(header.substring(BEARER_PREFIX.length()));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        try {
            JwtUser jwtUser = jwtService.parseToken(token);
            userRepository.findById(jwtUser.userId())
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .ifPresent(user -> {
                        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                        var authentication = new UsernamePasswordAuthenticationToken(
                                jwtUser,
                                null,
                                List.of(authority)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        } catch (InvalidJwtException ignored) {
            SecurityContextHolder.clearContext();
        }
    }
}
