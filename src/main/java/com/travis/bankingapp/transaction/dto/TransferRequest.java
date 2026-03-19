package com.travis.bankingapp.transaction.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferRequest {

  private String fromAccountNumber;
  private String toAccountNumber;
  private BigDecimal amount;
}
