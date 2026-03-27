package com.travis.bankingapp.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.travis.bankingapp.account.Account;
import com.travis.bankingapp.account.AccountRepository;
import com.travis.bankingapp.account.AccountType;
import com.travis.bankingapp.transaction.Transaction;
import com.travis.bankingapp.transaction.TransactionRepository;
import com.travis.bankingapp.transaction.TransactionType;

@SpringBootTest
class ExpiredUserCleanupServiceIntegrationTest {

  @Autowired
  private ExpiredUserCleanupService expiredUserCleanupService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private EntityManager entityManager;

  @BeforeEach
  void setUp() {
    transactionRepository.deleteAll();
    accountRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void usersOlderThan24HoursAreDeleted() {
    User expiredUser = createUserGraph("expired.user@example.com", OffsetDateTime.now().minusHours(25), true);

    expiredUserCleanupService.cleanupExpiredUsers();

    assertThat(userRepository.findById(expiredUser.getId())).isEmpty();
  }

  @Test
  void usersNewerThan24HoursAreNotDeleted() {
    User freshUser = createUserGraph("fresh.user@example.com", OffsetDateTime.now().minusHours(23), true);

    expiredUserCleanupService.cleanupExpiredUsers();

    assertThat(userRepository.findById(freshUser.getId())).isPresent();
  }

  @Test
  void deletingUserRemovesRelatedAccounts() {
    User user = createUserGraph("account.cascade@example.com", OffsetDateTime.now().minusHours(1), false);
    List<Account> accounts = accountRepository.findByUserId(user.getId());

    userRepository.delete(user);

    assertThat(accountRepository.findByUserId(user.getId())).isEmpty();
    assertThat(accounts).allSatisfy(account -> assertThat(accountRepository.findById(account.getId())).isEmpty());
  }

  @Test
  void deletingAccountRemovesRelatedTransactions() {
    User user = createUserGraph("transaction.cascade@example.com", OffsetDateTime.now().minusHours(1), true);
    Account account = accountRepository.findByUserId(user.getId()).getFirst();
    List<Transaction> transactions = transactionRepository.findByAccountId(account.getId());

    accountRepository.delete(account);

    assertThat(transactionRepository.findByAccountId(account.getId())).isEmpty();
    assertThat(transactions).allSatisfy(transaction -> assertThat(transactionRepository.findById(transaction.getId())).isEmpty());
  }

  @Test
  void cleanupDeletesUserAccountsAndTransactionsEndToEnd() {
    User expiredUser = createUserGraph("end.to.end@example.com", OffsetDateTime.now().minusHours(26), true);
    List<Account> accounts = accountRepository.findByUserId(expiredUser.getId());
    List<Long> accountIds = accounts.stream().map(Account::getId).toList();
    List<Long> transactionIds = accounts.stream()
      .flatMap(account -> transactionRepository.findByAccountId(account.getId()).stream())
      .map(Transaction::getId)
      .toList();

    expiredUserCleanupService.cleanupExpiredUsers();

    assertThat(userRepository.findById(expiredUser.getId())).isEmpty();
    assertThat(accountIds).allSatisfy(accountId -> assertThat(accountRepository.findById(accountId)).isEmpty());
    assertThat(transactionIds).allSatisfy(transactionId -> assertThat(transactionRepository.findById(transactionId)).isEmpty());
  }

  private User createUserGraph(String email, OffsetDateTime createdAt, boolean withTransactions) {
    User user = new User();
    user.setFirstName("Cleanup");
    user.setLastName("Test");
    user.setEmail(email);
    user.setPassword("password123");
    User savedUser = userRepository.save(user);

    Account checking = createAccount(savedUser, AccountType.CHECKING);
    Account savings = createAccount(savedUser, AccountType.SAVINGS);

    if (withTransactions) {
      createTransaction(checking, "Initial checking deposit");
      createTransaction(savings, "Initial savings deposit");
    }

    entityManager.flush();
    updateUserCreatedAt(savedUser.getId(), createdAt);
    entityManager.clear();

    return userRepository.findById(savedUser.getId()).orElseThrow();
  }

  private Account createAccount(User user, AccountType type) {
    Account account = new Account("ACC-" + System.nanoTime(), type, BigDecimal.ZERO);
    account.setUser(user);
    return accountRepository.save(account);
  }

  private void createTransaction(Account account, String description) {
    transactionRepository.save(new Transaction(TransactionType.DEPOSIT, BigDecimal.TEN, description, account));
  }

  private void updateUserCreatedAt(Long userId, OffsetDateTime createdAt) {
    entityManager.createNativeQuery("UPDATE users SET created_at = :createdAt WHERE id = :id")
      .setParameter("createdAt", createdAt)
      .setParameter("id", userId)
      .executeUpdate();
  }
}
