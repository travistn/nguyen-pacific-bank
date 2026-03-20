package com.travis.bankingapp.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.user.User;

@Component
public class AuthServiceHelper {

  public User getCurrentUser() {
    // get current authentication from security context
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // ensure an authenticated user exists
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
    }

    Object principal = authentication.getPrincipal();

    // ensure principal is actually User entity
    if (principal instanceof User user) {
      return user;
    }

    // if not, something is misconfigured in auth flow
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user");
  }

  // returns ID of currently authenticated user
  public Long getCurrentUserId() {
    return getCurrentUser().getId();
  }
}
