package com.travis.bankingapp.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.account.Account;
import com.travis.bankingapp.account.AccountRepository;
import com.travis.bankingapp.account.AccountType;
import com.travis.bankingapp.auth.dto.RegisterRequest;
import com.travis.bankingapp.user.User;
import com.travis.bankingapp.user.UserRepository;

@SpringBootTest
class AuthServiceIntegrationTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void successfulSignupCreatesExactlyTwoDefaultAccounts() {
    RegisterRequest request = registerRequest("new.user@example.com");

    User savedUser = authService.register(request);

    List<Account> accounts = accountRepository.findByUserId(savedUser.getId())
      .stream()
      .sorted(Comparator.comparing(Account::getType))
      .toList();

    assertThat(savedUser.getId()).isNotNull();
    assertThat(accounts).hasSize(2);
    assertThat(accounts)
      .extracting(Account::getType)
      .containsExactly(AccountType.CHECKING, AccountType.SAVINGS);
    assertThat(accounts)
      .allSatisfy(account -> assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO));
    assertThat(accounts)
      .extracting(account -> account.getUser().getId())
      .containsOnly(savedUser.getId());
  }

  @Test
  void duplicateEmailSignupDoesNotCreateAccounts() {
    RegisterRequest request = registerRequest("duplicate.user@example.com");
    authService.register(request);

    assertThatThrownBy(() -> authService.register(request))
      .isInstanceOf(ResponseStatusException.class)
      .extracting("statusCode.value")
      .isEqualTo(409);

    assertThat(userRepository.count()).isEqualTo(1);
    assertThat(accountRepository.count()).isEqualTo(2);
  }

  private RegisterRequest registerRequest(String email) {
    RegisterRequest request = new RegisterRequest();
    request.setFirstName("Test");
    request.setLastName("User");
    request.setEmail(email);
    request.setPassword("password123");
    return request;
  }
}
