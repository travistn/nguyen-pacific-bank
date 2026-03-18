package com.travis.bankingapp.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.travis.bankingapp.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String accountNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountType type;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal balance;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Account(String accountNumber, AccountType type, BigDecimal balance) {
    this.accountNumber = accountNumber;
    this.type = type;
    this.balance = balance;
  }

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }
}
