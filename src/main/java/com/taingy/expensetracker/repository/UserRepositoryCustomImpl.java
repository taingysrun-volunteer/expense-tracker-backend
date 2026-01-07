package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<User> findUsersWithFilters(
            String searchTerm,
            UUID roleId,
            Boolean isActive,
            Boolean isVerified,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        // Eagerly fetch the role to avoid lazy loading issues
        user.fetch("role", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // Search term (firstName, lastName, or email)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate firstNamePredicate = cb.like(cb.lower(user.get("firstName")), searchPattern);
            Predicate lastNamePredicate = cb.like(cb.lower(user.get("lastName")), searchPattern);
            Predicate emailPredicate = cb.like(cb.lower(user.get("email")), searchPattern);
            predicates.add(cb.or(firstNamePredicate, lastNamePredicate, emailPredicate));
        }

        // Filter by roleId
        if (roleId != null) {
            predicates.add(cb.equal(user.get("role").get("id"), roleId));
        }

        // Filter by isActive
        if (isActive != null) {
            predicates.add(cb.equal(user.get("isActive"), isActive));
        }

        // Filter by isVerified
        if (isVerified != null) {
            predicates.add(cb.equal(user.get("isVerified"), isVerified));
        }

        // Apply all predicates
        query.where(predicates.toArray(new Predicate[0]));

        // Make the query distinct to avoid duplicates from the join
        query.distinct(true);

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(user.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(user.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // Execute query with pagination
        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<User> users = typedQuery.getResultList();

        // Count query for total elements
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> userCount = countQuery.from(User.class);
        countQuery.select(cb.count(userCount));

        // Apply same predicates to count query
        List<Predicate> countPredicates = new ArrayList<>();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate firstNamePredicate = cb.like(cb.lower(userCount.get("firstName")), searchPattern);
            Predicate lastNamePredicate = cb.like(cb.lower(userCount.get("lastName")), searchPattern);
            Predicate emailPredicate = cb.like(cb.lower(userCount.get("email")), searchPattern);
            countPredicates.add(cb.or(firstNamePredicate, lastNamePredicate, emailPredicate));
        }
        if (roleId != null) {
            countPredicates.add(cb.equal(userCount.get("role").get("id"), roleId));
        }
        if (isActive != null) {
            countPredicates.add(cb.equal(userCount.get("isActive"), isActive));
        }
        if (isVerified != null) {
            countPredicates.add(cb.equal(userCount.get("isVerified"), isVerified));
        }

        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(users, pageable, total);
    }
}
