package com.rideshare.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: read the Authorization header
        // It should look like: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        final String authHeader = request.getHeader("Authorization");

        // Step 2: if no token or wrong format, skip — let Spring handle 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: remove "Bearer " prefix to get just the token
        final String jwt = authHeader.substring(7);
        final String userEmail = jwtUtil.extractUsername(jwt);

        // Step 4: if we got an email and user is not already authenticated
        if (userEmail != null &&
            SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 5: load user from the database
            var userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Step 6: validate the token
            if (jwtUtil.isTokenValid(jwt, userDetails)) {

                // Step 7: tell Spring Security this user is authenticated
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 8: pass to the next filter or the controller
        filterChain.doFilter(request, response);
    }
}