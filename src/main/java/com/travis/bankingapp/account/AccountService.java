package com.travis.bankingapp.account;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.user.User;
import com.travis.bankingapp.user.UserRepository;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final UserRepository userRepository;

  public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
    this.accountRepository = accountRepository;
    this.userRepository = userRepository;
  }

  public Account createAccount(Long userId, AccountType type) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    Account account = new Account(generateAccountNumber(), type, BigDecimal.ZERO);

    // link account to found user
    account.setUser(user);

    return accountRepository.save(account);
  }

  private String generateAccountNumber() {
    return String.valueOf(System.currentTimeMillis());
  }

  public List<Account> getAccountsByUser(Long userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    
    return accountRepository.findByUserId(userId);
  }

  public Account getAccountByNumber(String accountNumber) {
    return accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
  }

  // verify that user owns the account
  public void validateOwnership(Long userId, Account account) {
    if (!account.getUser().getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not own this account");
    }
  }

  public Account getUserAccount(Long userId, String accountNumber) {
    Account account = getAccountByNumber(accountNumber);

    validateOwnership(userId, account);

    return account;
  }
}
