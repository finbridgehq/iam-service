package com.liquilabs.vankoo.iam.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Value Object para el ID de un token de recuperación de contraseña.
 * <p>
 * Es un identificador de fila, no el secreto: se genera con UUIDv7 igual que
 * {@link UserId} y {@link RoleId}. El secreto que viaja en el correo se genera aparte
 * y con otra fuente, porque un UUIDv7 está ordenado en el tiempo y eso lo vuelve
 * adivinable — ver SecretTokenService.
 */
@Embeddable
public record PasswordResetTokenId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public PasswordResetTokenId {
        if (id == null) {
            throw new IllegalArgumentException("Password reset token ID cannot be null");
        }
    }

    public PasswordResetTokenId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
