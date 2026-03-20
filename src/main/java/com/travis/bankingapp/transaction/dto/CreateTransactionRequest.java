package com.travis.bankingapp.transaction.dto;

import java.math.BigDecimal;

import com.travis.bankingapp.transaction.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTransactionRequest {

  @NotBlank
  private String accountNumber;

  @NotNull
  private TransactionType type;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal amount;

  private String description;
}
