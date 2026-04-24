package com.travis.bankingapp.recurringtransaction;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travis.bankingapp.recurringtransaction.dto.CreateRecurringTransactionRequest;
import com.travis.bankingapp.recurringtransaction.dto.RecurringTransactionResponse;
import com.travis.bankingapp.recurringtransaction.dto.UpcomingRecurringTransactionResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/recurring-transaction", "/api/recurring-transactions"})
public class RecurringTransactionController {

  private final RecurringTransactionService recurringTransactionService;

  public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
    this.recurringTransactionService = recurringTransactionService;
  }

  @PostMapping
  public RecurringTransactionResponse createRecurringTransaction(@Valid @RequestBody CreateRecurringTransactionRequest request) {
    return recurringTransactionService.createRecurringTransaction(request);
  }

  @GetMapping
  public List<RecurringTransactionResponse> getRecurringTransactions() {
    return recurringTransactionService.getRecurringTransactions();
  }

  @GetMapping("/upcoming")
  public List<UpcomingRecurringTransactionResponse> getUpcomingRecurringTransactions() {
    return recurringTransactionService.getUpcomingRecurringTransactions();
  }

  @DeleteMapping("/{recurringTransactionId}")
  public ResponseEntity<Void> deleteRecurringTransaction(@PathVariable Long recurringTransactionId) {
    recurringTransactionService.deleteRecurringTransaction(recurringTransactionId);
    return ResponseEntity.noContent().build();
  }
}
