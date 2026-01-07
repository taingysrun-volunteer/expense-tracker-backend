package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.Expense;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class ExpenseRepositoryCustomImpl implements ExpenseRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Expense> findExpensesWithFilters(
            UUID userId,
            UUID categoryId,
            String searchTerm,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Expense> query = cb.createQuery(Expense.class);
        Root<Expense> expense = query.from(Expense.class);

        // Eagerly fetch related entities to avoid lazy loading issues
        expense.fetch("user", JoinType.LEFT);
        expense.fetch("category", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // Filter by userId
        if (userId != null) {
            predicates.add(cb.equal(expense.get("user").get("id"), userId));
        }

        // Filter by categoryId
        if (categoryId != null) {
            predicates.add(cb.equal(expense.get("category").get("id"), categoryId));
        }

        // Search term (title or description)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate titlePredicate = cb.like(cb.lower(expense.get("title")), searchPattern);
            Predicate descriptionPredicate = cb.like(cb.lower(expense.get("description")), searchPattern);
            predicates.add(cb.or(titlePredicate, descriptionPredicate));
        }

        // Filter by date range
        if (startDate != null && endDate != null) {
            predicates.add(cb.between(expense.get("expenseDate"), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(expense.get("expenseDate"), startDate));
        } else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(expense.get("expenseDate"), endDate));
        }

        // Filter by amount range
        if (minAmount != null && maxAmount != null) {
            predicates.add(cb.between(expense.get("amount"), minAmount, maxAmount));
        } else if (minAmount != null) {
            predicates.add(cb.greaterThanOrEqualTo(expense.get("amount"), minAmount));
        } else if (maxAmount != null) {
            predicates.add(cb.lessThanOrEqualTo(expense.get("amount"), maxAmount));
        }

        // Apply all predicates
        query.where(predicates.toArray(new Predicate[0]));

        // Make the query distinct to avoid duplicates from the joins
        query.distinct(true);

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(expense.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(expense.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // Execute query with pagination
        TypedQuery<Expense> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Expense> expenses = typedQuery.getResultList();

        // Count query for total elements
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Expense> expenseCount = countQuery.from(Expense.class);
        countQuery.select(cb.count(expenseCount));

        // Apply same predicates to count query
        List<Predicate> countPredicates = new ArrayList<>();
        if (userId != null) {
            countPredicates.add(cb.equal(expenseCount.get("user").get("id"), userId));
        }
        if (categoryId != null) {
            countPredicates.add(cb.equal(expenseCount.get("category").get("id"), categoryId));
        }
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate titlePredicate = cb.like(cb.lower(expenseCount.get("title")), searchPattern);
            Predicate descriptionPredicate = cb.like(cb.lower(expenseCount.get("description")), searchPattern);
            countPredicates.add(cb.or(titlePredicate, descriptionPredicate));
        }
        if (startDate != null && endDate != null) {
            countPredicates.add(cb.between(expenseCount.get("expenseDate"), startDate, endDate));
        } else if (startDate != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(expenseCount.get("expenseDate"), startDate));
        } else if (endDate != null) {
            countPredicates.add(cb.lessThanOrEqualTo(expenseCount.get("expenseDate"), endDate));
        }
        if (minAmount != null && maxAmount != null) {
            countPredicates.add(cb.between(expenseCount.get("amount"), minAmount, maxAmount));
        } else if (minAmount != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(expenseCount.get("amount"), minAmount));
        } else if (maxAmount != null) {
            countPredicates.add(cb.lessThanOrEqualTo(expenseCount.get("amount"), maxAmount));
        }

        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(expenses, pageable, total);
    }
}
