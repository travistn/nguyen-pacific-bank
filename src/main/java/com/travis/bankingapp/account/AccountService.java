package com.travis.bankingapp.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.account.dto.AccountResponse;
import com.travis.bankingapp.auth.AuthServiceHelper;
import com.travis.bankingapp.recurringtransaction.RecurringTransactionService;
import com.travis.bankingapp.user.User;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final AuthServiceHelper authServiceHelper;
  private final RecurringTransactionService recurringTransactionService;

  public AccountService(
    AccountRepository accountRepository,
    AuthServiceHelper authServiceHelper,
    RecurringTransactionService recurringTransactionService
  ) {
    this.accountRepository = accountRepository;
    this.authServiceHelper = authServiceHelper;
    this.recurringTransactionService = recurringTransactionService;
  }

  @Transactional
  public AccountResponse createAccount(AccountType type) {
    // get logged-in user from JWT authentication context
    User currentUser = authServiceHelper.getCurrentUser();
    Account savedAccount = createAccountForUser(currentUser, type);

    return mapToAccountResponse(savedAccount);
  }

  @Transactional
  public Account createAccountForUser(User user, AccountType type) {
    Account account = new Account(generateAccountNumber(), type, BigDecimal.ZERO);
    account.setUser(user);

    Account savedAccount = accountRepository.save(account);
    recurringTransactionService.seedNetflixRecurringWithdrawal(user, savedAccount);
    return savedAccount;
  }

  private String generateAccountNumber() {
    long timestamp = System.currentTimeMillis();
    int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);

    return timestamp + String.valueOf(suffix);
  }

  public List<AccountResponse> getAccountsForCurrentUser() {
    Long userId = authServiceHelper.getCurrentUserId();
    
    return accountRepository.findByUserId(userId)
      .stream()
      .map(this::mapToAccountResponse)
      .toList();
  }

  // retrieves an account by account number only if it belongs to the currently authenticated user
  public AccountResponse getAccountByNumber(String accountNumber) {
    Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

    Long currentUserId = authServiceHelper.getCurrentUserId();

    if (!account.getUser().getId().equals(currentUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    return mapToAccountResponse(account);
  }

  private AccountResponse mapToAccountResponse(Account account) {
    return new AccountResponse(account.getId(), account.getAccountNumber(), account.getBalance(), account.getType());
  }
}
