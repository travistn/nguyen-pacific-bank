package com.travis.bankingapp.account;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping
  public Account createAccount(@Valid @RequestBody CreateAccountRequest request) {
    return accountService.createAccount(request.getType());
  }

  // get all accounts for a user
  @GetMapping
  public List<Account> getAccountsForCurrentUser() {
    return accountService.getAccountsForCurrentUser();
  }

  // get one account by account number
  @GetMapping("/{accountNumber}")
  public Account getAccountByNumber(@PathVariable String accountNumber) {
    return accountService.getAccountByNumber(accountNumber);
  }
}
