package com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories;

import com.liquilabs.vankoo.iam.domain.model.aggregates.User;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UserId> {
    // Para el UserDetailServiceImpl
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UserId id);

    Optional<User> findByEmail(Email email);

    // Para el Sign-In: Trae Usuario + Roles en un solo SELECT para evitar el problema de N+1 queries
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") Email email);

    boolean existsByEmail(Email email);
}
