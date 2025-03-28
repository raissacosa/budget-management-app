package com.raissac.budget_management.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raissac.budget_management.category.controller.CategoryController;
import com.raissac.budget_management.category.dto.CategoryRequest;
import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.repository.CategoryRepository;
import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.exception.*;
import com.raissac.budget_management.handler.GlobalExceptionHandler;
import com.raissac.budget_management.security.config.CustomUserDetailsService;
import com.raissac.budget_management.security.config.JwtUtil;
import com.raissac.budget_management.security.entity.Role;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import com.raissac.budget_management.transaction.controller.TransactionController;
import com.raissac.budget_management.transaction.dto.*;
import com.raissac.budget_management.transaction.entity.Transaction;
import com.raissac.budget_management.transaction.entity.TransactionType;
import com.raissac.budget_management.transaction.repository.TransactionRepository;
import com.raissac.budget_management.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser(roles = "USER")
    @Test
    void createTransaction_shouldReturn200_whenTransactionRequestIsValid() throws Exception {

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(50.5),
                LocalDate.of(2025, 3, 2),
                "Sushi Restaurant",
                TransactionType.EXPENSE,
                1L);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction created successfully"));
    }

    @WithMockUser(roles = "USER")
    @Test
    void createTransaction_shouldReturn400_whenTransactionRequestIsInvalid() throws Exception {

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(-50.5),
                null,
                null,
                null,
                null);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "USER")
    @Test
    void createTransaction_shouldReturn404_whenCategoryNotFound() throws Exception {

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(50.5),
                LocalDate.of(2025, 3, 2),
                "Sushi Restaurant",
                TransactionType.EXPENSE,
                1L);


        when(transactionService.createTransaction(request))
                .thenThrow(new CategoryNotFoundException("Category not found"));


        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category not found"));
    }

    @WithMockUser(roles = "USER")
    @Test
    void createTransaction_shouldReturn404_whenUserNotFound() throws Exception {

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(50.5),
                LocalDate.of(2025, 3, 2),
                "Sushi Restaurant",
                TransactionType.EXPENSE,
                1L);


        when(transactionService.createTransaction(request))
                .thenThrow(new UserNotFoundException("User not found"));


        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }


    @Test
    void getAllTransactions_shouldReturn200WithPagedTransactions_whenRequestIsValid() throws Exception {
        TransactionFilterRequest filter = new TransactionFilterRequest(null, null, null, null, TransactionType.EXPENSE, null);

        List<TransactionResponse> responseList = List.of(
                new TransactionResponse(1L, new BigDecimal("150.00"),"Sushi Restaurant", LocalDate.now(), TransactionType.EXPENSE, "Food"),
                new TransactionResponse(2L, new BigDecimal("100.00"),"Sushi Groceries", LocalDate.now(), TransactionType.EXPENSE, "Food")
        );

        PageResponse<TransactionResponse> pageResponse = new PageResponse<>(
                responseList,
                0,
                responseList.size(),
                1,
                true,
                true
        );

        when(transactionService.findAllTransactions(any(TransactionFilterRequest.class), eq(0), eq(10)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].description").value("Sushi Restaurant"))
                .andExpect(jsonPath("$.content[1].categoryName").value("Food"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getAllTransactions_shouldReturn404_whenUserNotFound() throws Exception {
        when(transactionService.findAllTransactions(any(TransactionFilterRequest.class), eq(0), eq(10)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @WithMockUser(roles = "USER")
    @Test
    void deleteTransaction_shouldReturn200_whenTransactionDeletedSuccessfully() throws Exception {

        User user = User.builder()
                .id(1L)
                .firstName("User")
                .lastName("Test")
                .email("user.test@mail.com")
                .dateOfBirth(LocalDate.of(1998, 8, 2))
                .password("parola123")
                .role(Role.USER)
                .build();

        Category foodCategory = Category.builder().id(1L).name("Food").active(true).build();

        Transaction transaction = Transaction.builder()
                .id(99L)
                .amount(BigDecimal.valueOf(80.5))
                .description("Pizza Night")
                .date(LocalDate.of(2025, 2, 1))
                .type(TransactionType.EXPENSE)
                .category(foodCategory)
                .user(user)
                .build();
/*
        Transaction transaction = new Transaction();
        transaction.setId(99L);
        transaction.setUser(user);
*/
        when(userRepository.findByEmail("user.test@mail.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findById(99L)).thenReturn(Optional.of(transaction));

        mockMvc.perform(delete("/api/v1/transactions/99"))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction deleted successfully"));

        verify(transactionService).deleteTransaction(99L);
    }

    @WithMockUser(username = "user.test@mail.com", roles = "USER")
    @Test
    void deleteTransaction_shouldReturn404_whenTransactionNotFound() throws Exception {

        doThrow(new TransactionNotFoundException("Transaction with id: 99 not found"))
                .when(transactionService).deleteTransaction(99L);

        mockMvc.perform(delete("/api/v1/transactions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction with id: 99 not found"));
    }

    @WithMockUser(username = "user.test@mail.com", roles = "USER")
    @Test
    void deleteTransaction_shouldReturn401_whenTransactionNotOwnedByUser() throws Exception {
        doThrow(new AccessDeniedException("You are not allowed to delete this transaction"))
                .when(transactionService).deleteTransaction(99L);

        mockMvc.perform(delete("/api/v1/transactions/99"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("You are not allowed to delete this transaction"));
    }

    @WithMockUser(username = "user@test.com", roles = "USER")
    @Test
    void getSpentByCategory_shouldReturnTotalSpentPerCategory_whenUserIsAuthenticated() throws Exception {

        List<TotalSpentPerCategoryResponse> response = List.of(
                new TotalSpentPerCategoryResponse("Food", new BigDecimal("150.00")),
                new TotalSpentPerCategoryResponse("Travel", new BigDecimal("250.00"))
        );

        when(transactionService.getTotalSpentPerCategory()).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/expenses/by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].totalSpent").value(150.00))
                .andExpect(jsonPath("$[1].categoryName").value("Travel"))
                .andExpect(jsonPath("$[1].totalSpent").value(250.00));
    }

    @WithMockUser(username = "user@test.com", roles = "USER")
    @Test
    void getAccountBalance_shouldReturnAccountBalance_whenUserIsAuthenticated() throws Exception {

        BalanceResponse response = new BalanceResponse(new BigDecimal("2500.00"),new BigDecimal("1500.00"),new BigDecimal("1000.00"));


        when(transactionService.getAccountBalance()).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(2500.00))
                .andExpect(jsonPath("$.totalExpenses").value(1500.00))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }


    @WithMockUser(username = "user@test.com", roles = "USER")
    @Test
    void getMonthlySummary_shouldReturnMonthlySummary_whenUserIsAuthenticated() throws Exception {

        List<MonthlyTransactionSummaryResponse> response = List.of(
                new MonthlyTransactionSummaryResponse(Month.JANUARY, new BigDecimal("2500.00"), new BigDecimal("1500.00")),
                new MonthlyTransactionSummaryResponse(Month.FEBRUARY, new BigDecimal("3000.00"), new BigDecimal("1400.00"))
        );

        when(transactionService.getMonthlySummary(2025)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/summary/monthly").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].month").value("JANUARY"))
                .andExpect(jsonPath("$[0].income").value(2500.00))
                .andExpect(jsonPath("$[0].expenses").value(1500.00))
                .andExpect(jsonPath("$[1].month").value("FEBRUARY"))
                .andExpect(jsonPath("$[1].income").value(3000.00))
                .andExpect(jsonPath("$[1].expenses").value(1400.00));
    }


    @WithMockUser(username = "user@test.com", roles = "USER")
    @Test
    void getTopSpendingCategories_shouldReturnTopSendingCategories_whenUserIsAuthenticated() throws Exception {

        List<TopSpendingCategoryResponse> response = List.of(
                new TopSpendingCategoryResponse("Food", new BigDecimal("1500.00")),
                new TopSpendingCategoryResponse("Travel", new BigDecimal("3000.00"))
        );


        when(transactionService.getTopSpendingCategories()).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/expenses/top-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].totalSpent").value(1500.00))
                .andExpect(jsonPath("$[1].categoryName").value("Travel"))
                .andExpect(jsonPath("$[1].totalSpent").value(3000.00));
    }

    @WithMockUser(username = "user@test.com", roles = "USER")
    @Test
    void exportCSV_shouldReturnCSVFile_whenExportIsSuccessful() throws Exception {
        String csvContent = """
            ID,Amount,Date,Description,Type,Category\r
            1,80.5,2025-02-01,Pizza Night,EXPENSE,Food\r
            """;

        ByteArrayInputStream csvStream = new ByteArrayInputStream(csvContent.getBytes());

        when(transactionService.exportTransactionsToCSV()).thenReturn(csvStream);

        mockMvc.perform(get("/api/v1/transactions/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv"))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(csvContent));
    }

    @WithMockUser(username = "user@test.com", roles = "USER")
    @Test
    void exportCSV_shouldReturn500_whenCsvExportFails() throws Exception {
        when(transactionService.exportTransactionsToCSV())
                .thenThrow(new CsvExportException("Failed to write CSV"));

        mockMvc.perform(get("/api/v1/transactions/export"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to write CSV"))
                .andExpect(jsonPath("$.errorDescription").value("CSV export error!"));
    }
}
