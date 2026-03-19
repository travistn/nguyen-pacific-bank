package com.travis.bankingapp.transaction.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {

  private String fromAccountNumber;
  private String toAccountNumber;
  private BigDecimal amount;
}
