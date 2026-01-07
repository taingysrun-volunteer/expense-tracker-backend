package com.taingy.expensetracker.controller;

import com.taingy.expensetracker.dto.ExpenseRequest;
import com.taingy.expensetracker.dto.ExpenseResponse;
import com.taingy.expensetracker.dto.ExpenseSummary;
import com.taingy.expensetracker.dto.ResponseMessage;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.UserRepository;
import com.taingy.expensetracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    @Autowired
    public ExpenseController(ExpenseService expenseService, UserRepository userRepository) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> findAll(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Get current user to check role
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // If user has USER role, restrict to their own expenses only
        UUID effectiveUserId = userId;
        if ("USER".equals(currentUser.getRole().getName())) {
            effectiveUserId = currentUser.getId();
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(expenseService.findExpensesWithFilters(
                effectiveUserId, categoryId, searchTerm, startDate, endDate, minAmount, maxAmount, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> findById(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        ExpenseResponse expenseResponse = expenseService.findById(id);

        if (expenseResponse == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }

        // If user has USER role, verify they own this expense
        if ("USER".equals(currentUser.getRole().getName()) && !expenseResponse.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok(expenseResponse);
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@RequestBody ExpenseRequest expenseRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        ExpenseResponse expenseResponse = expenseService.create(expenseRequest, userEmail);
        return ResponseEntity.ok(expenseResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable UUID id, @RequestBody ExpenseRequest expenseRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Verify ownership for USER role
        ExpenseResponse existingExpense = expenseService.findById(id);
        if (existingExpense == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }

        if ("USER".equals(currentUser.getRole().getName()) && !existingExpense.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        ExpenseResponse expenseResponse = expenseService.update(id, expenseRequest);
        return ResponseEntity.ok(expenseResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> delete(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Verify ownership for USER role
        ExpenseResponse existingExpense = expenseService.findById(id);
        if (existingExpense == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }

        if ("USER".equals(currentUser.getRole().getName()) && !existingExpense.getUserId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        expenseService.delete(id);
        return ResponseEntity.ok(new ResponseMessage("Successfully deleted Expense"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummary> getSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        ExpenseSummary summary = expenseService.getSummary(userEmail);
        return ResponseEntity.ok(summary);
    }

}
