package com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories;

import com.liquilabs.vankoo.iam.domain.model.aggregates.PasswordResetToken;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.PasswordResetTokenId;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.TokenDigest;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, PasswordResetTokenId> {

    // Un índice único: validar un enlace es una sola búsqueda, cueste lo que cueste el
    // resultado. Que el token exista o no no cambia el tiempo de respuesta.
    Optional<PasswordResetToken> findByTokenDigest(TokenDigest tokenDigest);

    // Cada cuenta tiene como mucho un enlace vivo: pedir uno nuevo borra el anterior.
    // Si no, quien pidió un enlace hace media hora lo conserva funcionando después de
    // que la víctima pida el suyo.
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.userId = :userId")
    int deleteAllByUserId(@Param("userId") UserId userId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now OR t.consumedAt IS NOT NULL")
    int deleteExpiredOrConsumed(@Param("now") Date now);
}
