package com.travis.bankingapp.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.travis.bankingapp.account.Account;
import com.travis.bankingapp.account.AccountRepository;
import com.travis.bankingapp.auth.dto.RegisterRequest;
import com.travis.bankingapp.user.UserRepository;

@SpringBootTest
class AuthServiceTransactionRollbackTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @MockitoSpyBean
  private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void signupRollsBackIfAccountCreationFails() {
    AtomicInteger saveCalls = new AtomicInteger();
    doAnswer(invocation -> {
      if (saveCalls.incrementAndGet() == 1) {
        Account account = invocation.getArgument(0);
        entityManager.persist(account);
        entityManager.flush();
        return account;
      }

      throw new RuntimeException("account persistence failed");
    }).when(accountRepository).save(any(Account.class));

    RegisterRequest request = registerRequest("rollback.user@example.com");

    assertThatThrownBy(() -> authService.register(request))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("account persistence failed");

    assertThat(userRepository.findByEmail(request.getEmail())).isEmpty();
    assertThat(accountRepository.count()).isZero();
  }

  private RegisterRequest registerRequest(String email) {
    RegisterRequest request = new RegisterRequest();
    request.setFirstName("Rollback");
    request.setLastName("User");
    request.setEmail(email);
    request.setPassword("password123");
    return request;
  }
}
