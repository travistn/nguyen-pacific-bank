package com.travis.bankingapp.recurringtransaction;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travis.bankingapp.recurringtransaction.dto.RecurringTransactionResponse;
import com.travis.bankingapp.recurringtransaction.dto.UpcomingRecurringTransactionResponse;

@RestController
@RequestMapping({"/api/recurring-transaction", "/api/recurring-transactions"})
public class RecurringTransactionController {

  private final RecurringTransactionService recurringTransactionService;

  public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
    this.recurringTransactionService = recurringTransactionService;
  }

  @PostMapping
  public RecurringTransactionResponse createRecurringNetflixWithdrawal() {
    return recurringTransactionService.createRecurringNetflixWithdrawal();
  }

  @GetMapping
  public RecurringTransactionResponse getRecurringNetflixWithdrawal() {
    return recurringTransactionService.getRecurringNetflixWithdrawal();
  }

  @GetMapping("/upcoming")
  public List<UpcomingRecurringTransactionResponse> getUpcomingRecurringTransactions() {
    return recurringTransactionService.getUpcomingRecurringTransactions();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteRecurringNetflixWithdrawal() {
    recurringTransactionService.deleteRecurringNetflixWithdrawal();
    return ResponseEntity.noContent().build();
  }
}
