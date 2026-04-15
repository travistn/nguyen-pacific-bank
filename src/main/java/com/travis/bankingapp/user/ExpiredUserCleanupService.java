package com.travis.bankingapp.user;

// import java.time.OffsetDateTime;
// import java.util.List;

// import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpiredUserCleanupService {

  // private static final long EXPIRATION_HOURS = 24;

  private final UserRepository userRepository;

  public ExpiredUserCleanupService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // @Scheduled(fixedRate = 60 * 60 * 1000)
  @Transactional
  public void cleanupExpiredUsers() {
    // OffsetDateTime cutoff = OffsetDateTime.now().minusHours(EXPIRATION_HOURS);
    // List<User> expiredUsers = userRepository.findByCreatedAtBefore(cutoff);
    //
    // if (!expiredUsers.isEmpty()) {
    //   userRepository.deleteAll(expiredUsers);
    // }
  }
}
