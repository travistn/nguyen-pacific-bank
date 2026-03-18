package com.travis.bankingapp.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.travis.bankingapp.account.Account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionType type;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  private String description;

  @ManyToOne(optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Transaction(TransactionType type, BigDecimal amount, String description, Account account) {
    this.type = type;
    this.amount = amount;
    this.description = description;
    this.account = account;
  }

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }
}
