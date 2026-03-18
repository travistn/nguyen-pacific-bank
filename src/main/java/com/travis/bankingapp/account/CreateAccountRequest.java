package com.travis.bankingapp.account;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

  @NotNull
  private Long userId;

  @NotNull
  private AccountType type;
}
