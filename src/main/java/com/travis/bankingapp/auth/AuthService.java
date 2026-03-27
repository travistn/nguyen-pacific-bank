package com.travis.bankingapp.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.account.AccountService;
import com.travis.bankingapp.account.AccountType;
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
  private final AccountService accountService;

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService,
    AccountService accountService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.accountService = accountService;
  }

  // handles user registration
  @Transactional
  public User register(RegisterRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    User savedUser = userRepository.save(user);
    accountService.createAccountForUser(savedUser, AccountType.CHECKING);
    accountService.createAccountForUser(savedUser, AccountType.SAVINGS);

    return savedUser;
  }

  // handles user login
  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

    // verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    String token = jwtService.generateToken(user.getEmail());

    return new AuthResponse(token);
  }
}
