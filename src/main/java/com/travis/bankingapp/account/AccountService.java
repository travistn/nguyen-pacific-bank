package com.travis.bankingapp.account;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.auth.AuthServiceHelper;
import com.travis.bankingapp.user.User;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final AuthServiceHelper authServiceHelper;

  public AccountService(AccountRepository accountRepository, AuthServiceHelper authServiceHelper) {
    this.accountRepository = accountRepository;
    this.authServiceHelper = authServiceHelper;
  }

  public Account createAccount(AccountType type) {
    // get logged-in user from JWT authentication context
    User currentUser = authServiceHelper.getCurrentUser();

    Account account = new Account(generateAccountNumber(), type, BigDecimal.ZERO);

    // // link account to authenticated user
    account.setUser(currentUser);

    return accountRepository.save(account);
  }

  private String generateAccountNumber() {
    return String.valueOf(System.currentTimeMillis());
  }

  public List<Account> getAccountsForCurrentUser() {
    Long userId = authServiceHelper.getCurrentUserId();
    
    return accountRepository.findByUserId(userId);
  }

  // retrieves an account by account number only if it belongs to the currently authenticated user
  public Account getAccountByNumber(String accountNumber) {
    Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

    Long currentUserId = authServiceHelper.getCurrentUserId();

    if (!account.getUser().getId().equals(currentUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    return account;
  }
}
