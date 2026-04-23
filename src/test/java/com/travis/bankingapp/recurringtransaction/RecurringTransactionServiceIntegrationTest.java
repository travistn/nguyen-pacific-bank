package com.travis.bankingapp.recurringtransaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.account.Account;
import com.travis.bankingapp.account.AccountRepository;
import com.travis.bankingapp.account.AccountType;
import com.travis.bankingapp.recurringtransaction.dto.RecurringTransactionResponse;
import com.travis.bankingapp.recurringtransaction.dto.UpcomingRecurringTransactionResponse;
import com.travis.bankingapp.transaction.Transaction;
import com.travis.bankingapp.transaction.TransactionRepository;
import com.travis.bankingapp.transaction.TransactionType;
import com.travis.bankingapp.user.User;
import com.travis.bankingapp.user.UserRepository;

@SpringBootTest
class RecurringTransactionServiceIntegrationTest {

  @Autowired
  private RecurringTransactionService recurringTransactionService;

  @Autowired
  private RecurringTransactionScheduler recurringTransactionScheduler;

  @Autowired
  private RecurringTransactionRepository recurringTransactionRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    transactionRepository.deleteAll();
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
  void createRecurringTransactionSuccessfullyWhenUserHasCheckingAccount() {
    User user = createUser("create.success@example.com");
    createAccount(user, AccountType.CHECKING, BigDecimal.ZERO);
    authenticate(user);

    RecurringTransactionResponse response = recurringTransactionService.createRecurringNetflixWithdrawal();

    assertThat(response.getId()).isNotNull();
    assertThat(response.getDescription()).isEqualTo("Netflix");
    assertThat(response.getAmount()).isEqualByComparingTo("20.00");
    assertThat(response.getDayOfMonth()).isEqualTo(20);
    assertThat(response.isActive()).isTrue();
    assertThat(response.getAccountType()).isEqualTo(AccountType.CHECKING);
    assertThat(recurringTransactionRepository.findByUserId(user.getId())).isPresent();
  }

  @Test
  void rejectCreateWhenNoCheckingAccountExists() {
    User user = createUser("no.checking@example.com");
    createAccount(user, AccountType.SAVINGS, BigDecimal.ZERO);
    authenticate(user);

    assertThatThrownBy(() -> recurringTransactionService.createRecurringNetflixWithdrawal())
      .isInstanceOf(ResponseStatusException.class)
      .extracting("statusCode.value")
      .isEqualTo(400);
  }

  @Test
  void rejectCreateWhenRecurringTransactionAlreadyExists() {
    User user = createUser("duplicate.recurring@example.com");
    createAccount(user, AccountType.CHECKING, BigDecimal.ZERO);
    authenticate(user);
    recurringTransactionService.createRecurringNetflixWithdrawal();

    assertThatThrownBy(() -> recurringTransactionService.createRecurringNetflixWithdrawal())
      .isInstanceOf(ResponseStatusException.class)
      .extracting("statusCode.value")
      .isEqualTo(409);
  }

  @Test
  void returnCurrentRecurringTransactionForAuthenticatedUser() {
    User user = createUser("get.recurring@example.com");
    createAccount(user, AccountType.CHECKING, BigDecimal.ZERO);
    authenticate(user);
    RecurringTransactionResponse created = recurringTransactionService.createRecurringNetflixWithdrawal();

    RecurringTransactionResponse response = recurringTransactionService.getRecurringNetflixWithdrawal();

    assertThat(response.getId()).isEqualTo(created.getId());
    assertThat(response.getDescription()).isEqualTo("Netflix");
  }

  @Test
  void deleteRecurringTransactionSuccessfully() {
    User user = createUser("delete.recurring@example.com");
    createAccount(user, AccountType.CHECKING, BigDecimal.ZERO);
    authenticate(user);
    recurringTransactionService.createRecurringNetflixWithdrawal();

    recurringTransactionService.deleteRecurringNetflixWithdrawal();

    assertThat(recurringTransactionRepository.findByUserId(user.getId())).isEmpty();
  }

  @Test
  void getUpcomingRecurringTransactionsReturnsComputedNextRunDate() {
    User user = createUser("upcoming.recurring@example.com");
    Account checking = createAccount(user, AccountType.CHECKING, BigDecimal.ZERO);
    LocalDate today = LocalDate.now();
    int dayOfMonth = today.getDayOfMonth() == 1 ? 2 : today.getDayOfMonth() - 1;
    recurringTransactionRepository.save(new RecurringTransaction(
      user,
      checking,
      "Netflix",
      new BigDecimal("20.00"),
      dayOfMonth,
      OffsetDateTime.now()
    ));
    authenticate(user);

    List<UpcomingRecurringTransactionResponse> response = recurringTransactionService.getUpcomingRecurringTransactions();

    LocalDate expectedNextRunDate = today.withDayOfMonth(dayOfMonth).plusMonths(1);
    assertThat(response).hasSize(1);
    assertThat(response.getFirst().getName()).isEqualTo("Netflix");
    assertThat(response.getFirst().getAmount()).isEqualByComparingTo("20.00");
    assertThat(response.getFirst().getNextRunDate()).isEqualTo(expectedNextRunDate);
    assertThat(response.getFirst().getAccountId()).isEqualTo(checking.getId());
  }

  @Test
  void getUpcomingRecurringTransactionsReturnsEmptyListWhenNoneExist() {
    User user = createUser("upcoming.empty@example.com");
    authenticate(user);

    List<UpcomingRecurringTransactionResponse> response = recurringTransactionService.getUpcomingRecurringTransactions();

    assertThat(response).isEmpty();
  }

  @Test
  void processDueRecurringTransactionCreatesWithdrawalAndAdvancesNextRunAt() {
    User user = createUser("process.success@example.com");
    Account checking = createAccount(user, AccountType.CHECKING, new BigDecimal("100.00"));
    OffsetDateTime dueAt = OffsetDateTime.now().minusMonths(1);
    RecurringTransaction recurringTransaction = recurringTransactionRepository.save(new RecurringTransaction(
      user,
      checking,
      "Netflix",
      new BigDecimal("20.00"),
      20,
      dueAt
    ));

    recurringTransactionService.processDueRecurringTransaction(recurringTransaction.getId(), OffsetDateTime.now());

    Account updatedChecking = accountRepository.findById(checking.getId()).orElseThrow();
    List<Transaction> transactions = transactionRepository.findByAccountId(checking.getId());
    RecurringTransaction updatedRecurring = recurringTransactionRepository.findById(recurringTransaction.getId()).orElseThrow();
    assertThat(updatedChecking.getBalance()).isEqualByComparingTo("80.00");
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().getDescription()).isEqualTo("Netflix");
    assertThat(transactions.getFirst().getAmount()).isEqualByComparingTo("20.00");
    assertThat(transactions.getFirst().getType()).isEqualTo(TransactionType.WITHDRAWAL);
    assertThat(updatedRecurring.getNextRunAt()).isCloseTo(dueAt.plusMonths(1), within(1, ChronoUnit.MICROS));
  }

  @Test
  void insufficientFundsSkipsTransactionAndStillMovesNextRunAtForward() {
    User user = createUser("insufficient.recurring@example.com");
    Account checking = createAccount(user, AccountType.CHECKING, new BigDecimal("10.00"));
    OffsetDateTime dueAt = OffsetDateTime.now().minusMonths(1);
    RecurringTransaction recurringTransaction = recurringTransactionRepository.save(new RecurringTransaction(
      user,
      checking,
      "Netflix",
      new BigDecimal("20.00"),
      20,
      dueAt
    ));

    recurringTransactionService.processDueRecurringTransaction(recurringTransaction.getId(), OffsetDateTime.now());

    Account updatedChecking = accountRepository.findById(checking.getId()).orElseThrow();
    RecurringTransaction updatedRecurring = recurringTransactionRepository.findById(recurringTransaction.getId()).orElseThrow();
    assertThat(updatedChecking.getBalance()).isEqualByComparingTo("10.00");
    assertThat(transactionRepository.findByAccountId(checking.getId())).isEmpty();
    assertThat(updatedRecurring.getNextRunAt()).isCloseTo(dueAt.plusMonths(1), within(1, ChronoUnit.MICROS));
  }

  @Test
  void notDueRecurringTransactionDoesNotCreateDuplicateWithdrawal() {
    User user = createUser("not.due@example.com");
    Account checking = createAccount(user, AccountType.CHECKING, new BigDecimal("100.00"));
    OffsetDateTime futureRunAt = OffsetDateTime.now().plusDays(1);
    RecurringTransaction recurringTransaction = recurringTransactionRepository.save(new RecurringTransaction(
      user,
      checking,
      "Netflix",
      new BigDecimal("20.00"),
      20,
      futureRunAt
    ));

    recurringTransactionService.processDueRecurringTransaction(recurringTransaction.getId(), OffsetDateTime.now());

    Account updatedChecking = accountRepository.findById(checking.getId()).orElseThrow();
    RecurringTransaction updatedRecurring = recurringTransactionRepository.findById(recurringTransaction.getId()).orElseThrow();
    assertThat(updatedChecking.getBalance()).isEqualByComparingTo("100.00");
    assertThat(transactionRepository.findByAccountId(checking.getId())).isEmpty();
    assertThat(updatedRecurring.getNextRunAt()).isCloseTo(futureRunAt, within(1, ChronoUnit.MICROS));
  }

  @Test
  void schedulerProcessesDueRecurringTransaction() {
    User user = createUser("scheduler.due@example.com");
    Account checking = createAccount(user, AccountType.CHECKING, new BigDecimal("100.00"));
    OffsetDateTime dueAt = OffsetDateTime.now().minusDays(1);
    recurringTransactionRepository.save(new RecurringTransaction(
      user,
      checking,
      "Netflix",
      new BigDecimal("20.00"),
      20,
      dueAt
    ));

    recurringTransactionScheduler.processDueRecurringTransactions();

    Account updatedChecking = accountRepository.findById(checking.getId()).orElseThrow();
    List<Transaction> transactions = transactionRepository.findByAccountId(checking.getId());
    assertThat(updatedChecking.getBalance()).isEqualByComparingTo("80.00");
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().getDescription()).isEqualTo("Netflix");
  }

  private User createUser(String email) {
    User user = new User();
    user.setFirstName("Recurring");
    user.setLastName("Test");
    user.setEmail(email);
    user.setPassword("password123");
    return userRepository.save(user);
  }

  private Account createAccount(User user, AccountType type, BigDecimal balance) {
    Account account = new Account("ACC-" + System.nanoTime(), type, balance);
    account.setUser(user);
    return accountRepository.save(account);
  }

  private void authenticate(User user) {
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
