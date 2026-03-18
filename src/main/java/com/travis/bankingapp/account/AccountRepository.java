package com.travis.bankingapp.account;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

  List<Account> findByUserId(long user_id);

  Optional<Account> findByAccountNumber(String accountNumber);
}
