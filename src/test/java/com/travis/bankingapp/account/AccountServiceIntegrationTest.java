package com.travis.bankingapp.account;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.travis.bankingapp.recurringtransaction.RecurringTransaction;
import com.travis.bankingapp.recurringtransaction.RecurringTransactionRepository;
import com.travis.bankingapp.user.User;
import com.travis.bankingapp.user.UserRepository;

@SpringBootTest
class AccountServiceIntegrationTest {

  @Autowired
  private AccountService accountService;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private RecurringTransactionRepository recurringTransactionRepository;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    recurringTransactionRepository.deleteAll();
    accountRepository.deleteAll();
    userRepository.deleteAll();
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createCheckingAccountSeedsNetflixRecurringTransaction() {
    User user = createUser("account.seed@example.com");
    authenticate(user);

    accountService.createAccount(AccountType.CHECKING);

    RecurringTransaction recurringTransaction = recurringTransactionRepository.findByUserId(user.getId()).orElseThrow();
    assertThat(recurringTransaction.getDescription()).isEqualTo("Netflix");
    assertThat(recurringTransaction.getAmount()).isEqualByComparingTo("20.00");
    assertThat(recurringTransaction.getDayOfMonth()).isEqualTo(20);
    assertThat(recurringTransaction.getAccount().getType()).isEqualTo(AccountType.CHECKING);
  }

  @Test
  void createAdditionalAccountsDoesNotCreateDuplicateNetflixRecurringTransactions() {
    User user = createUser("account.no-duplicate@example.com");
    authenticate(user);

    accountService.createAccount(AccountType.CHECKING);
    accountService.createAccount(AccountType.SAVINGS);
    accountService.createAccount(AccountType.CHECKING);

    assertThat(recurringTransactionRepository.count()).isEqualTo(1);
  }

  private User createUser(String email) {
    User user = new User();
    user.setFirstName("Account");
    user.setLastName("Tester");
    user.setEmail(email);
    user.setPassword("password123");
    return userRepository.save(user);
  }

  private void authenticate(User user) {
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
