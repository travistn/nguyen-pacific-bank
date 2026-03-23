package com.travis.bankingapp.account.dto;

import com.travis.bankingapp.account.AccountType;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {

  @NotNull
  private AccountType type;
}
