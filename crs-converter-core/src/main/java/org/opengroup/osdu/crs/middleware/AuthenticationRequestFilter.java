package org.opengroup.osdu.crs.middleware;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationRequestFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    public AuthenticationRequestFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        if (authenticationService.isAuthorized(httpServletRequest, httpServletResponse)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }
}
