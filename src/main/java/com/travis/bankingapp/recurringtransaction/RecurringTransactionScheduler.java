package com.travis.bankingapp.recurringtransaction;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionScheduler {

  private final RecurringTransactionService recurringTransactionService;

  public RecurringTransactionScheduler(RecurringTransactionService recurringTransactionService) {
    this.recurringTransactionService = recurringTransactionService;
  }

  @Scheduled(fixedDelay = 60000)
  public void processDueRecurringTransactions() {
    OffsetDateTime now = OffsetDateTime.now();

    recurringTransactionService.getDueRecurringTransactionIds(now)
      .forEach(id -> recurringTransactionService.processDueRecurringTransaction(id, now));
  }
}
