package com.travis.bankingapp.auth;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService customUserDetailsService;

  public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
    this.jwtService = jwtService;
    this.customUserDetailsService = customUserDetailsService;
  }

  // checks for JWT in Authorization header then authenticates user
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    
    // get Authorization header from request
    final String authHeader = request.getHeader("Authorization");

    final String jwt;
    final String email;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // extract token by removing "Bearer " prefix
    jwt = authHeader.substring(7);

    // extract email (username) from token payload
    email = jwtService.extractEmail(jwt);

    // if email exists AND user is not already authenticated
    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      // Load user details from database
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

      // validate token and confirm it belongs to user
      if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

        // create an authentication token for Spring Security
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // attach request-specific details (IP address, session info, etc)
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // store authentication in SecurityContext
        // this tells SpringSecurity that user is now authenticated
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    // continue processing the rest of the filter chain
    filterChain.doFilter(request, response);
  }
}
