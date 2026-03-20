package com.travis.bankingapp.transaction;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.travis.bankingapp.account.Account;
import com.travis.bankingapp.account.AccountRepository;
import com.travis.bankingapp.auth.AuthServiceHelper;
import com.travis.bankingapp.transaction.dto.TransferRequest;

import jakarta.transaction.Transactional;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final AuthServiceHelper authServiceHelper;

  public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, AuthServiceHelper authServiceHelper) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.authServiceHelper = authServiceHelper;
  }

  // ensures that the currently authenticated user owns the given account
  private void validateCurrentUserOwnership(Account account) {
    Long currentUserId = authServiceHelper.getCurrentUserId();

    if (!account.getUser().getId().equals(currentUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
  } 

  // creates a new transaction for an account owned by the currently authenticated user
  @Transactional
  public Transaction createTransaction(String accountNumber, TransactionType type, BigDecimal amount, String description) {
    Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

    // ensure the logged-in user owns this account
    validateCurrentUserOwnership(account);

    // validate the amount is positive
    validateAmount(amount);

    if (type == TransactionType.WITHDRAWAL && account.getBalance().compareTo(amount) < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
    }

    updateAccountBalance(account, type, amount);

    Transaction transaction = new Transaction(type, amount, description, account);

    accountRepository.save(account);

    return transactionRepository.save(transaction);
  }

  // retrieves all transactions for an account owned by the currently authenticated user
  public List<Transaction> getTransactionsByAccount(String accountNumber) {
    Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

    // ensure the logged-in user owns this account
    validateCurrentUserOwnership(account);

    return transactionRepository.findByAccountId(account.getId());
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

  // transfers funds from one account to another
  @Transactional
  public void transfer(TransferRequest request) {

    // validate transfer amount
    validateAmount(request.getAmount());

    // prevent self-transfer
    if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to the same account");
    }

    // retrieve source (sender) account
    Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));

    // ensure the logged-in user owns the source account
    validateCurrentUserOwnership(fromAccount);

    // retrieve destination (receiver) account
    Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

    // ensure sufficient funds
    if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
    }

    // subtract from sender
    fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));

    // add to receiver
    toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

    // save updated balances
    accountRepository.save(fromAccount);
    accountRepository.save(toAccount);

    // create outgoing transaction record
    Transaction transferOut = new Transaction(
      TransactionType.TRANSFER_OUT,
      request.getAmount(),
      "Transfer to account " + toAccount.getAccountNumber(),
      fromAccount
    );

    // create incoming transaction record
    Transaction transferIn = new Transaction(
      TransactionType.TRANSFER_IN,
      request.getAmount(),
      "Transfer from account " + fromAccount.getAccountNumber(),
      toAccount
    );

    // save both transactions
    transactionRepository.save(transferOut);
    transactionRepository.save(transferIn);
  }
}
