package com.travis.bankingapp.recurringtransaction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.travis.bankingapp.account.AccountType;

import lombok.Getter;

@Getter
public class RecurringTransactionResponse {

  private final Long id;
  private final String description;
  private final BigDecimal amount;
  private final Integer dayOfMonth;
  private final OffsetDateTime nextRunAt;
  private final boolean active;
  private final AccountType accountType;

  public RecurringTransactionResponse(Long id, String description, BigDecimal amount, Integer dayOfMonth, OffsetDateTime nextRunAt, boolean active, AccountType accountType) {
    this.id = id;
    this.description = description;
    this.amount = amount;
    this.dayOfMonth = dayOfMonth;
    this.nextRunAt = nextRunAt;
    this.active = active;
    this.accountType = accountType;
  }
}
