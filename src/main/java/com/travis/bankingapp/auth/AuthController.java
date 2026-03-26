package com.travis.bankingapp.auth;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travis.bankingapp.auth.dto.AuthResponse;
import com.travis.bankingapp.auth.dto.LoginRequest;
import com.travis.bankingapp.auth.dto.RegisterRequest;
import com.travis.bankingapp.user.User;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public User register(@RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
    @RequestBody LoginRequest request,
    HttpServletResponse response
  ) {
    AuthResponse authResponse = authService.login(request);

    String jwt = authResponse.getToken();

    ResponseCookie cookie = ResponseCookie.from("token", jwt)
      .httpOnly(true)
      .secure(true)
      .path("/")
      .maxAge(86400) // 1 day
      .sameSite("None")
      .build();

    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, cookie.toString())
      .body(Map.of("message", "Login successful"));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    ResponseCookie cookie = ResponseCookie.from("token", "")
      .httpOnly(true)
      .secure(true)
      .path("/")
      .maxAge(0) // expires cookie immediately
      .sameSite("None")
      .build();

    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, cookie.toString())
      .body(Map.of("message", "Logout successful"));
  }
}
