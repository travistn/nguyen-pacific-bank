package com.travis.bankingapp.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
  
  private String firstName;
  private String lastName;
  private String email;
  private String password;
}
