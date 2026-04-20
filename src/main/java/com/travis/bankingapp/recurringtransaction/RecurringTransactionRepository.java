package com.travis.bankingapp.recurringtransaction;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

  Optional<RecurringTransaction> findByUserId(Long userId);

  boolean existsByUserId(Long userId);

  List<RecurringTransaction> findByActiveTrueAndNextRunAtLessThanEqual(OffsetDateTime now);
}
