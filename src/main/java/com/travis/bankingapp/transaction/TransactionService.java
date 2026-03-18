package com.travis.bankingapp.transaction;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.account.Account;
import com.travis.bankingapp.account.AccountRepository;

import jakarta.transaction.Transactional;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
  }

  // creates a new transaction for an account
  @Transactional
  public Transaction createTransaction(Long accountId, TransactionType type, BigDecimal amount, String description) {
    Account account = accountRepository.findById(accountId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

    validateAmount(amount);

    if (type == TransactionType.WITHDRAWAL && account.getBalance().compareTo(amount) < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
    }

    updateAccountBalance(account, type, amount);

    Transaction transaction = new Transaction(type, amount, description, account);

    accountRepository.save(account);

    return transactionRepository.save(transaction);
  }

  // retrieves all transactions associated with a specific account
  public List<Transaction> getTransactionsByAccount(Long accountId) {
    accountRepository.findById(accountId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

    return transactionRepository.findByAccountId(accountId);
  }

  // validates that the transaction amount is greater than zero
  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
    }
  }

  // updates account balance according to the transaction type
  private void updateAccountBalance(Account account, TransactionType type, BigDecimal amount) {
    switch (type) {
      case DEPOSIT:
        account.setBalance(account.getBalance().add(amount));
        break;
      
        case WITHDRAWAL:
          account.setBalance(account.getBalance().subtract(amount));
          break;
        
        default:
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported transaction type");
    }
  }
}
