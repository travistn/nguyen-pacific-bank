package com.travis.bankingapp.account;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {

  @NotNull
  private Long userId;

  @NotNull
  private AccountType type;
}
