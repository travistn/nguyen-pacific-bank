package com.travis.bankingapp.transaction;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travis.bankingapp.transaction.dto.CreateTransactionRequest;
import com.travis.bankingapp.transaction.dto.TransferRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
  public Transaction createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
    return transactionService.createTransaction(
      request.getAccountNumber(),
      request.getType(),
      request.getAmount(),
      request.getDescription());
  }

  @GetMapping("/account/{accountNumber}")
    public List<Transaction> getTransactionsByAccount(@PathVariable String accountNumber) {
      return transactionService.getTransactionsByAccount(accountNumber);
    }
 
  @PostMapping("/transfer")
  public String transfer(@Valid @RequestBody TransferRequest request) {
    transactionService.transfer(request);
    return "Transfer completed successfully";
  }
}
