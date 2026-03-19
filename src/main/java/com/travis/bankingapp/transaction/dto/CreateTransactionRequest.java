package com.travis.bankingapp.transaction.dto;

import java.math.BigDecimal;

import com.travis.bankingapp.transaction.TransactionType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTransactionRequest {

  @NotNull
  private Long accountId;

  @NotNull
  private TransactionType type;

  private BigDecimal amount;

  private String description;
}
