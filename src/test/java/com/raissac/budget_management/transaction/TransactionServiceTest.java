package com.raissac.budget_management.transaction;

import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.repository.CategoryRepository;
import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.exception.*;
import com.raissac.budget_management.security.entity.Role;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import com.raissac.budget_management.transaction.dto.*;
import com.raissac.budget_management.transaction.entity.Transaction;
import com.raissac.budget_management.transaction.entity.TransactionType;
import com.raissac.budget_management.transaction.repository.TransactionRepository;
import com.raissac.budget_management.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    private User user;

    private Category travelCategory;

    private Category foodCategory;

    private Transaction transaction1;

    private Transaction transaction2;

    private Transaction transaction3;

    private Transaction transaction4;

    private boolean initialized = false;

    @BeforeEach
    void setup() {
        if (!initialized) {
            cleanupDatabase();
            setupUser();
            setupCategories();
            setupTransactions();
            initialized = true;
        }

        authenticateUser("user.test@mail.com");
    }

    private void cleanupDatabase() {
        categoryRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    private void setupUser() {
        user = userRepository.save(User.builder()
                .firstName("User")
                .lastName("Test")
                .email("user.test@mail.com")
                .dateOfBirth(LocalDate.of(1998, 8, 2))
                .password("parola123")
                .role(Role.USER)
                .build());
    }

    private void setupCategories() {
        travelCategory = categoryRepository.save(Category.builder().name("Travel").active(true).build());
        foodCategory = categoryRepository.save(Category.builder().name("Food").active(true).build());
        categoryRepository.save(Category.builder().name("Entertainment").active(false).build());
    }

    private void setupTransactions() {
        transaction1 = Transaction.builder()
                .amount(BigDecimal.valueOf(80.5))
                .description("Pizza Night")
                .date(LocalDate.of(2025, 2, 1))
                .type(TransactionType.EXPENSE)
                .category(foodCategory)
                .user(user)
                .build();

        transaction2 = Transaction.builder()
                .amount(BigDecimal.valueOf(100.5))
                .description("Sushi Restaurant")
                .date(LocalDate.of(2025, 3, 2))
                .type(TransactionType.EXPENSE)
                .category(foodCategory)
                .user(user)
                .build();

        transaction3 = Transaction.builder()
                .amount(BigDecimal.valueOf(40))
                .description("Vatican Museum")
                .date(LocalDate.of(2025, 2, 1))
                .type(TransactionType.EXPENSE)
                .category(travelCategory)
                .user(user)
                .build();

        transaction4 = Transaction.builder()
                .amount(BigDecimal.valueOf(40))
                .description("Cashback card")
                .date(LocalDate.of(2025, 1, 20))
                .type(TransactionType.INCOME)
                .category(travelCategory)
                .user(user)
                .build();
    }

    private void authenticateUser(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null));
    }

    @Test
    void shouldSaveTransaction_whenCategoryRequestIsValid() {

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(50.5),
                LocalDate.of(2025, 2, 1),
                "Airplane tickets to Rome",
                TransactionType.EXPENSE,
                travelCategory.getId()
        );

        Transaction saved = transactionService.createTransaction(request);

        assertNotNull(saved.getId());
        assertEquals(request.amount(), saved.getAmount());
        assertEquals(request.description(), saved.getDescription());
        assertEquals(request.type(), saved.getType());
        assertEquals(request.categoryId(), saved.getCategory().getId());
    }

    @Test
    void shouldThrowException_whenUserNotFound() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user2@mail.com", null)
        );

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(50.5),
                LocalDate.of(2025,2,1),
                "Airplane tickets to Rome",
                TransactionType.EXPENSE,
                1L
        );

        assertThrows(UserNotFoundException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void shouldThrowException_whenCategoryNotFound() {

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(50.5),
                LocalDate.of(2025,2,1),
                "Airplane tickets to Rome",
                TransactionType.EXPENSE,
                999L
        );

        assertThrows(CategoryNotFoundException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void shouldReturnPagedTransactions_whenFindAllIsCalled() {

        transactionRepository.deleteAll();

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);

        TransactionFilterRequest request = new TransactionFilterRequest(BigDecimal.valueOf(10), BigDecimal.valueOf(100), null, null, null, foodCategory.getId());

        PageResponse<TransactionResponse> response = transactionService.findAllTransactions(request, 0, 3);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void shouldThrowExceptionUserNotFound_whenFindAllIsCalled() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user2@mail.com", null)
        );
        TransactionFilterRequest request = new TransactionFilterRequest(BigDecimal.valueOf(10), BigDecimal.valueOf(100), null, null, null, foodCategory.getId());

        assertThrows(UserNotFoundException.class, () -> transactionService.findAllTransactions(request, 0, 3));
    }

    @Test
    void shouldDeleteTransaction_whenUserIsOwner() {

        Transaction transactionDelete = transactionRepository.save(
                Transaction.builder()
                        .amount(BigDecimal.valueOf(50))
                        .description("Test")
                        .date(LocalDate.now())
                        .type(TransactionType.EXPENSE)
                        .user(user)
                        .category(travelCategory)
                        .build()
        );

        transactionService.deleteTransaction(transactionDelete.getId());

        assertFalse(transactionRepository.findById(transactionDelete.getId()).isPresent());
    }

    @Test
    void shouldThrowException_whenTransactionNotFound() {

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.deleteTransaction(999L));
    }

    @Test
    void shouldThrowException_whenUserIsNotOwner() {

        transactionRepository.deleteAll();

        Transaction transaction = transactionRepository.save(transaction1);

        User notOwner = userRepository.save(
                User.builder().firstName("User")
                        .lastName("Test")
                        .email("not.owner@mail.com")
                        .dateOfBirth(LocalDate.of(1998,8,2))
                        .password("parola123")
                        .role(Role.USER).build()
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not.owner@mail.com", null)
        );

        assertThrows(AccessDeniedException.class,
                () -> transactionService.deleteTransaction(transaction.getId()));
    }

    @Test
    void shouldThrowExceptionUserNotFound_whenDeleteTransactionIsCalled() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user2@mail.com", null)
        );

        assertThrows(UserNotFoundException.class,
                () -> transactionService.deleteTransaction(1L));
    }

    @Test
    void shouldReturnCorrectBalance_forUserWithTransactions() {

        transactionRepository.deleteAll();

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);
        transactionRepository.save(transaction4);

        BalanceResponse response = transactionService.getAccountBalance();

        assertEquals(0, BigDecimal.valueOf(40.0).compareTo(response.totalIncome()));
        assertEquals(0, BigDecimal.valueOf(221.0).compareTo(response.totalExpenses()));
        assertEquals(0, BigDecimal.valueOf(-181.0).compareTo(response.balance()));
    }

    @Test
    void shouldTotalSpentPerCategory_forUserWithTransactions() {

        transactionRepository.deleteAll();

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);
        transactionRepository.save(transaction4);

        List<TotalSpentPerCategoryResponse> response = transactionService.getTotalSpentPerCategory();

        assertEquals(2, response.size());
        assertEquals("Food", response.get(0).categoryName());
        assertEquals(0, BigDecimal.valueOf(181.0).compareTo(response.get(0).totalSpent()));
        assertEquals("Travel", response.get(1).categoryName());
        assertEquals(0, BigDecimal.valueOf(40.0).compareTo(response.get(1).totalSpent()));


    }

    @Test
    void shouldReturnTopSpendingCategories_forUserWithTransactions() {

        transactionRepository.deleteAll();

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);
        transactionRepository.save(transaction4);

        List<TopSpendingCategoryResponse> response = transactionService.getTopSpendingCategories();

        assertEquals(2, response.size());
        assertEquals("Food", response.get(0).categoryName());
        assertEquals(0, BigDecimal.valueOf(181.0).compareTo(response.get(0).totalSpent()));
        assertEquals("Travel", response.get(1).categoryName());
        assertEquals(0, BigDecimal.valueOf(40.0).compareTo(response.get(1).totalSpent()));

    }

    @Test
    void shouldReturnMonthlySummary_forUserWithTransactions() {

        transactionRepository.deleteAll();

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);
        transactionRepository.save(transaction4);

        List<MonthlyTransactionSummaryResponse> response = transactionService.getMonthlySummary(2025);

        assertEquals("FEBRUARY",response.get(1).month().toString());
        assertEquals(0, BigDecimal.valueOf(120.5).compareTo(response.get(1).expenses()));
        assertEquals("JANUARY",response.get(0).month().toString());
        assertEquals(0, BigDecimal.valueOf(40.0).compareTo(response.get(0).income()));

    }

    @Test
    void shouldExportTransactionsToCSV_forUserWithTransactions() {

        transactionRepository.deleteAll();

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);
        transactionRepository.save(transaction4);

        ByteArrayInputStream csvStream = transactionService.exportTransactionsToCSV();

        assertNotNull(csvStream);

        String csvContent = new String(csvStream.readAllBytes());

        assertTrue(csvContent.contains("ID,Amount,Date,Description,Type,Category"));
        assertTrue(csvContent.contains("80.5,2025-02-01,Pizza Night,EXPENSE,Food"));

    }

}