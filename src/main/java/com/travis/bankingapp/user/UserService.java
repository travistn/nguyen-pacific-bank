package com.travis.bankingapp.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.auth.AuthServiceHelper;
import com.travis.bankingapp.auth.dto.RegisterRequest;

import java.util.List;


@Service
public class UserService {
  
  private final UserRepository userRepository;
  private final AuthServiceHelper authServiceHelper;

  public UserService(UserRepository userRepository, AuthServiceHelper authServiceHelper) {
    this.userRepository = userRepository;
    this.authServiceHelper = authServiceHelper;
  }

  public User createUser(RegisterRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setPassword(request.getPassword());

    return userRepository.save(user);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public void deleteCurrentUser() {
    Long currentUserId = authServiceHelper.getCurrentUserId();
    User user = userRepository.findById(currentUserId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    userRepository.delete(user);
  }
}


