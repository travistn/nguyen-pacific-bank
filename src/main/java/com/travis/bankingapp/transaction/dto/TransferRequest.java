package com.travis.bankingapp.transaction.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {

  @NotBlank
  private String fromAccountNumber;

  @NotBlank
  private String toAccountNumber;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal amount;
}
