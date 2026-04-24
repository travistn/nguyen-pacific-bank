package com.travis.bankingapp.recurringtransaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.travis.bankingapp.account.AccountRepository;
import com.travis.bankingapp.auth.AuthService;
import com.travis.bankingapp.auth.JwtService;
import com.travis.bankingapp.auth.dto.RegisterRequest;
import com.travis.bankingapp.transaction.TransactionRepository;
import com.travis.bankingapp.user.User;
import com.travis.bankingapp.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class RecurringTransactionControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AuthService authService;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private RecurringTransactionRepository recurringTransactionRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    transactionRepository.deleteAll();
    recurringTransactionRepository.deleteAll();
    accountRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void authenticatedPostCreatesRecurringTransactionWithoutRemovingSeededNetflixTransaction() throws Exception {
    User user = registerUser("controller.post@example.com");
    String token = jwtService.generateToken(user.getEmail());
    Long accountId = accountRepository.findByUserId(user.getId()).stream()
      .filter(account -> account.getType().name().equals("CHECKING"))
      .findFirst()
      .orElseThrow()
      .getId();

    mockMvc.perform(post("/api/recurring-transaction")
        .header("Authorization", "Bearer " + token)
        .contentType("application/json")
        .content("""
          {
            "accountId": %d,
            "description": "Gym",
            "amount": 45.00,
            "dayOfMonth": 12
          }
          """.formatted(accountId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.description").value("Gym"))
      .andExpect(jsonPath("$.amount").value(45.00))
      .andExpect(jsonPath("$.dayOfMonth").value(12));

    assertThat(recurringTransactionRepository.findAllByUserId(user.getId())).hasSize(2);
  }

  @Test
  void authenticatedGetReturnsRecurringTransactions() throws Exception {
    User user = registerUser("controller.get@example.com");
    String token = jwtService.generateToken(user.getEmail());

    mockMvc.perform(get("/api/recurring-transaction")
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].description").value("Netflix"))
      .andExpect(jsonPath("$[0].amount").value(20.00));
  }

  @Test
  void authenticatedGetUpcomingReturnsUpcomingRecurringTransactions() throws Exception {
    User user = registerUser("controller.upcoming@example.com");
    String token = jwtService.generateToken(user.getEmail());
    LocalDate today = LocalDate.now();
    LocalDate expectedNextRunDate = today.getDayOfMonth() <= 20
      ? today.withDayOfMonth(20)
      : today.withDayOfMonth(20).plusMonths(1);

    mockMvc.perform(get("/api/recurring-transactions/upcoming")
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].name").value("Netflix"))
      .andExpect(jsonPath("$[0].amount").value(20.00))
      .andExpect(jsonPath("$[0].nextRunDate").value(expectedNextRunDate.toString()))
      .andExpect(jsonPath("$[0].accountId").isNumber());
  }

  @Test
  void authenticatedDeleteRemovesRecurringTransaction() throws Exception {
    User user = registerUser("controller.delete@example.com");
    String token = jwtService.generateToken(user.getEmail());
    Long recurringTransactionId = recurringTransactionRepository.findAllByUserId(user.getId()).getFirst().getId();

    mockMvc.perform(delete("/api/recurring-transaction/{recurringTransactionId}", recurringTransactionId)
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isNoContent());

    assertThat(recurringTransactionRepository.findAllByUserId(user.getId())).isEmpty();
  }

  @Test
  void unauthorizedRequestsAreRejected() throws Exception {
    mockMvc.perform(post("/api/recurring-transaction"))
      .andExpect(status().isForbidden());

    mockMvc.perform(get("/api/recurring-transaction"))
      .andExpect(status().isForbidden());

    mockMvc.perform(get("/api/recurring-transactions/upcoming"))
      .andExpect(status().isForbidden());

    mockMvc.perform(delete("/api/recurring-transaction/1"))
      .andExpect(status().isForbidden());
  }

  private User registerUser(String email) {
    RegisterRequest request = new RegisterRequest();
    request.setFirstName("Controller");
    request.setLastName("Test");
    request.setEmail(email);
    request.setPassword("password123");
    return authService.register(request);
  }
}
