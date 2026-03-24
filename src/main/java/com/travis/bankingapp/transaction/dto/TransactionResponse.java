package com.travis.bankingapp.transaction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.travis.bankingapp.account.AccountType;
import com.travis.bankingapp.transaction.TransactionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionResponse {
  
  private Long id;
  private BigDecimal amount;
  private TransactionType type;
  private String description;
  private OffsetDateTime transactionDate;
  private AccountType accountType;

  public TransactionResponse(Long id, BigDecimal amount, TransactionType type, String description, OffsetDateTime transactionDate, AccountType accountType) {
    this.id = id;
    this.amount = amount; 
    this.type = type;
    this.description = description;
    this.transactionDate = transactionDate;
    this.accountType = accountType;
  }
}
