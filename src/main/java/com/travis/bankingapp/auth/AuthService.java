package com.travis.bankingapp.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.travis.bankingapp.auth.dto.AuthResponse;
import com.travis.bankingapp.auth.dto.LoginRequest;
import com.travis.bankingapp.auth.dto.RegisterRequest;
import com.travis.bankingapp.user.User;
import com.travis.bankingapp.user.UserRepository;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  // handles user registration
  public User register(RegisterRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    return userRepository.save(user);
  }

  // handles user login
  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    // verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Invalid email or password");
    }

    String token = jwtService.generateToken(user.getEmail());

    return new AuthResponse(token);
  }
}
