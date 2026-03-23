package com.travis.bankingapp.account.dto;

import java.math.BigDecimal;

import com.travis.bankingapp.account.AccountType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponse {

  private Long id;
  private String accountNumber;
  private BigDecimal balance;
  private AccountType type;

  public AccountResponse(Long id, String accountNumber, BigDecimal balance, AccountType type) {
    this.id = id;
    this.accountNumber = accountNumber;
    this.balance = balance;
    this.type = type;
  }
}
