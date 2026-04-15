package com.travis.bankingapp.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.travis.bankingapp.account.Account;
import com.travis.bankingapp.account.AccountRepository;
import com.travis.bankingapp.account.AccountType;
import com.travis.bankingapp.transaction.Transaction;
import com.travis.bankingapp.transaction.TransactionRepository;
import com.travis.bankingapp.transaction.TransactionType;

@SpringBootTest
class DeleteCurrentUserIntegrationTest {

  @Autowired
  private UserController userController;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    transactionRepository.deleteAll();
    accountRepository.deleteAll();
    userRepository.deleteAll();
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void deleteCurrentUserReturnsNoContentAndRemovesAuthenticatedUserAccountsAndTransactions() {
    User user = createUserGraph("delete.me@example.com");
    User otherUser = createUserGraph("keep.me@example.com");
    List<Long> accountIds = accountRepository.findByUserId(user.getId())
      .stream()
      .map(Account::getId)
      .toList();
    List<Long> transactionIds = accountIds.stream()
      .flatMap(accountId -> transactionRepository.findByAccountId(accountId).stream())
      .map(Transaction::getId)
      .toList();
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    var response = userController.deleteCurrentUser();

    assertThat(response.getStatusCode().value()).isEqualTo(204);
    assertThat(userRepository.findById(user.getId())).isEmpty();
    assertThat(accountIds).allSatisfy(accountId -> assertThat(accountRepository.findById(accountId)).isEmpty());
    assertThat(transactionIds).allSatisfy(transactionId -> assertThat(transactionRepository.findById(transactionId)).isEmpty());
    assertThat(userRepository.findById(otherUser.getId())).isPresent();
    assertThat(accountRepository.findByUserId(otherUser.getId())).hasSize(2);
  }

  private User createUserGraph(String email) {
    User user = new User();
    user.setFirstName("Delete");
    user.setLastName("Test");
    user.setEmail(email);
    user.setPassword("password123");
    User savedUser = userRepository.save(user);

    Account checking = createAccount(savedUser, AccountType.CHECKING);
    Account savings = createAccount(savedUser, AccountType.SAVINGS);
    createTransaction(checking, "Initial checking deposit");
    createTransaction(savings, "Initial savings deposit");

    return savedUser;
  }

  private Account createAccount(User user, AccountType type) {
    Account account = new Account("ACC-" + System.nanoTime(), type, BigDecimal.ZERO);
    account.setUser(user);
    return accountRepository.save(account);
  }

  private void createTransaction(Account account, String description) {
    transactionRepository.save(new Transaction(TransactionType.DEPOSIT, BigDecimal.TEN, description, account));
  }
}
