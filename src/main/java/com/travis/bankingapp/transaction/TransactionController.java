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

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
  public Transaction createTransaction(@RequestBody CreateTransactionRequest request) {
    return transactionService.createTransaction(
      request.getAccountId(),
      request.getType(),
      request.getAmount(),
      request.getDescription());
  }

  @GetMapping("account/{accountId}")
    public List<Transaction> getTransactionsByAccount(@PathVariable Long accountId) {
      return transactionService.getTransactionsByAccount(accountId);
    }
 
  @PostMapping("/transfer")
  public String transfer(@RequestBody TransferRequest request) {
    transactionService.transfer(request);
    return "Transfer completed successfully";
  }
}
