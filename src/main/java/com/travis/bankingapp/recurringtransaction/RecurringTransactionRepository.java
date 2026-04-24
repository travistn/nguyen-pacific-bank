package com.travis.bankingapp.recurringtransaction;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

  List<RecurringTransaction> findAllByUserId(Long userId);

  List<RecurringTransaction> findAllByUserIdOrderByCreatedAtAsc(Long userId);

  boolean existsByUserIdAndDescriptionIgnoreCase(Long userId, String description);

  List<RecurringTransaction> findByActiveTrueAndNextRunAtLessThanEqual(OffsetDateTime now);
}
