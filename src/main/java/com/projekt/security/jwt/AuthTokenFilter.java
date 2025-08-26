package com.projekt.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.security.services.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthTokenFilter extends OncePerRequestFilter {
    private final JWTUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/**",
            "/api/categories/**",
            "/api/priorities/**",
            "/api/statuses/**",
            "/api/software/**",
            "/api/knowledge-bases/**"
    );

    public AuthTokenFilter(JWTUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return PUBLIC_PATHS.stream().anyMatch(p -> {
            boolean pathMatches = pathMatcher.match(p, request.getServletPath());
            if (p.equals("/api/auth/**")) {
                return pathMatches;
            }

            return pathMatches && "GET".equals(request.getMethod());
        });
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired: {}", e);
            handleException(response, "JWT token has expired", request.getServletPath());
            return;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e);
            handleException(response, "Invalid JWT token", request.getServletPath());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleException(HttpServletResponse response, String message, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", message);
        body.put("path", path);

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
