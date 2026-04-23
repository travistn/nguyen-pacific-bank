package com.travis.bankingapp.recurringtransaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpcomingRecurringTransactionResponse {

  private String name;
  private BigDecimal amount;
  private LocalDate nextRunDate;
  private Long accountId;

  public UpcomingRecurringTransactionResponse(String name, BigDecimal amount, LocalDate nextRunDate, Long accountId) {
    this.name = name;
    this.amount = amount;
    this.nextRunDate = nextRunDate;
    this.accountId = accountId;
  }
}
