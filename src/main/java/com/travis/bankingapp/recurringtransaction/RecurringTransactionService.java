package com.travis.bankingapp.recurringtransaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.account.Account;
import com.travis.bankingapp.account.AccountRepository;
import com.travis.bankingapp.account.AccountType;
import com.travis.bankingapp.auth.AuthServiceHelper;
import com.travis.bankingapp.recurringtransaction.dto.RecurringTransactionResponse;
import com.travis.bankingapp.transaction.TransactionService;
import com.travis.bankingapp.transaction.TransactionType;
import com.travis.bankingapp.user.User;

@Service
public class RecurringTransactionService {

  public static final String NETFLIX_DESCRIPTION = "Netflix";
  public static final BigDecimal NETFLIX_AMOUNT = new BigDecimal("20.00");
  public static final int NETFLIX_DAY_OF_MONTH = 20;

  private final RecurringTransactionRepository recurringTransactionRepository;
  private final AccountRepository accountRepository;
  private final AuthServiceHelper authServiceHelper;
  private final TransactionService transactionService;

  public RecurringTransactionService(
    RecurringTransactionRepository recurringTransactionRepository,
    AccountRepository accountRepository,
    AuthServiceHelper authServiceHelper,
    TransactionService transactionService
  ) {
    this.recurringTransactionRepository = recurringTransactionRepository;
    this.accountRepository = accountRepository;
    this.authServiceHelper = authServiceHelper;
    this.transactionService = transactionService;
  }

  @Transactional
  public RecurringTransactionResponse createRecurringNetflixWithdrawal() {
    User currentUser = authServiceHelper.getCurrentUser();
    Account checkingAccount = findCheckingAccountForUser(currentUser.getId());

    if (recurringTransactionRepository.existsByUserId(currentUser.getId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Recurring Netflix withdrawal already exists");
    }

    RecurringTransaction recurringTransaction = buildNetflixRecurringTransaction(currentUser, checkingAccount, OffsetDateTime.now());

    return mapToResponse(recurringTransactionRepository.save(recurringTransaction));
  }

  @Transactional
  public void seedNetflixRecurringWithdrawal(User user, Account account) {
    if (!AccountType.CHECKING.equals(account.getType()) || recurringTransactionRepository.existsByUserId(user.getId())) {
      return;
    }

    RecurringTransaction recurringTransaction = buildNetflixRecurringTransaction(user, account, OffsetDateTime.now());
    recurringTransactionRepository.save(recurringTransaction);
  }

  public RecurringTransactionResponse getRecurringNetflixWithdrawal() {
    Long currentUserId = authServiceHelper.getCurrentUserId();

    return recurringTransactionRepository.findByUserId(currentUserId)
      .map(this::mapToResponse)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurring Netflix withdrawal not found"));
  }

  @Transactional
  public void deleteRecurringNetflixWithdrawal() {
    Long currentUserId = authServiceHelper.getCurrentUserId();
    RecurringTransaction recurringTransaction = recurringTransactionRepository.findByUserId(currentUserId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurring Netflix withdrawal not found"));

    recurringTransactionRepository.delete(recurringTransaction);
  }

  public List<Long> getDueRecurringTransactionIds(OffsetDateTime now) {
    return recurringTransactionRepository.findByActiveTrueAndNextRunAtLessThanEqual(now)
      .stream()
      .map(RecurringTransaction::getId)
      .toList();
  }

  @Transactional
  public void processDueRecurringTransaction(Long recurringTransactionId, OffsetDateTime now) {
    RecurringTransaction recurringTransaction = recurringTransactionRepository.findById(recurringTransactionId).orElse(null);

    if (recurringTransaction == null || !recurringTransaction.isActive() || recurringTransaction.getNextRunAt().isAfter(now)) {
      return;
    }

    Account account = recurringTransaction.getAccount();
    if (account == null || account.getId() == null || accountRepository.findById(account.getId()).isEmpty()) {
      recurringTransaction.setActive(false);
      recurringTransactionRepository.save(recurringTransaction);
      return;
    }

    if (account.getBalance().compareTo(NETFLIX_AMOUNT) >= 0) {
      transactionService.createTransactionForAccount(
        account,
        TransactionType.WITHDRAWAL,
        NETFLIX_AMOUNT,
        NETFLIX_DESCRIPTION
      );
    }

    recurringTransaction.setNextRunAt(calculateNextRunAtAfter(recurringTransaction.getNextRunAt()));
    recurringTransactionRepository.save(recurringTransaction);
  }

  private Account findCheckingAccountForUser(Long userId) {
    return accountRepository.findByUserId(userId)
      .stream()
      .filter(account -> AccountType.CHECKING.equals(account.getType()))
      .findFirst()
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Checking account not found"));
  }

  private RecurringTransaction buildNetflixRecurringTransaction(User user, Account account, OffsetDateTime now) {
    return new RecurringTransaction(
      user,
      account,
      NETFLIX_DESCRIPTION,
      NETFLIX_AMOUNT,
      NETFLIX_DAY_OF_MONTH,
      calculateNextRunAt(now)
    );
  }

  private OffsetDateTime calculateNextRunAt(OffsetDateTime now) {
    OffsetDateTime scheduledThisMonth = now.withDayOfMonth(NETFLIX_DAY_OF_MONTH)
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .withNano(0);

    if (now.getDayOfMonth() <= NETFLIX_DAY_OF_MONTH) {
      return scheduledThisMonth;
    }

    return scheduledThisMonth.plusMonths(1);
  }

  private OffsetDateTime calculateNextRunAtAfter(OffsetDateTime currentNextRunAt) {
    return currentNextRunAt.plusMonths(1);
  }

  private RecurringTransactionResponse mapToResponse(RecurringTransaction recurringTransaction) {
    return new RecurringTransactionResponse(
      recurringTransaction.getId(),
      recurringTransaction.getDescription(),
      recurringTransaction.getAmount(),
      recurringTransaction.getDayOfMonth(),
      recurringTransaction.getNextRunAt(),
      recurringTransaction.isActive(),
      recurringTransaction.getAccount().getType()
    );
  }
}
