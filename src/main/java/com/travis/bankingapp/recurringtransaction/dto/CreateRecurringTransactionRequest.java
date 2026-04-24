package com.travis.bankingapp.recurringtransaction.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRecurringTransactionRequest {

  @NotNull
  private Long accountId;

  @NotBlank
  private String description;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal amount;

  @NotNull
  @Min(1)
  @Max(28)
  private Integer dayOfMonth;
}
