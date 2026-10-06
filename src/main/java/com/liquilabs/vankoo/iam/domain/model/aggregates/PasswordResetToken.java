package com.liquilabs.vankoo.iam.domain.model.aggregates;

import com.liquilabs.vankoo.iam.domain.model.events.PasswordResetRequestedEvent;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.PasswordResetTokenId;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.TokenDigest;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.UserId;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * Un permiso de un solo uso para cambiar la contraseña de una cuenta.
 * <p>
 * Es un agregado aparte y no tres columnas en {@link User}. Colgarlo del usuario
 * metería campos nulables en la tabla que se lee en cada inicio de sesión, y pedir un
 * enlace pasaría a modificar la fila del usuario —incluido su updatedAt de auditoría—
 * cuando en realidad no le ha cambiado nada.
 * <p>
 * La referencia al usuario es por identidad, no una asociación @ManyToOne: un token no
 * es parte del usuario, y así no hay carga perezosa que se dispare sola ni cascadas
 * que borren lo que no deben.
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
        @Index(name = "idx_password_reset_token_digest", columnList = "token_digest", unique = true),
        @Index(name = "idx_password_reset_token_user_id", columnList = "user_id")
})
public class PasswordResetToken extends AbstractAggregateRoot<PasswordResetToken>
        implements Persistable<PasswordResetTokenId> {

    @EmbeddedId
    private PasswordResetTokenId id;

    @Embedded
    private TokenDigest tokenDigest;

    /**
     * El @AttributeOverride no es cosmético. La columna que declara UserId lleva
     * unique = true, que es correcto en la tabla de usuarios y sería un error aquí:
     * dejaría a cada cuenta con una única fila de por vida y rompería el reemplazo del
     * enlace en cuanto alguien pidiera el segundo.
     */
    @Embedded
    @AttributeOverride(name = "id",
            column = @Column(name = "user_id", columnDefinition = "UUID", nullable = false))
    private UserId userId;

    @Column(nullable = false)
    private Date expiresAt;

    /** Cuándo se usó. Nulo mientras siga vivo; una fecha dice más que un booleano. */
    @Column
    private Date consumedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

    protected PasswordResetToken() {}

    public PasswordResetToken(UserId userId, TokenDigest tokenDigest, Date expiresAt) {
        this.id = new PasswordResetTokenId();
        this.userId = userId;
        this.tokenDigest = tokenDigest;
        this.expiresAt = expiresAt;
    }

    /** Sirve una sola vez y solo dentro de su ventana. */
    public boolean isUsable(Date at) {
        return consumedAt == null && expiresAt.after(at);
    }

    public void consume(Date at) {
        this.consumedAt = at;
    }

    /**
     * El secreto en claro viaja en el evento y no en la fila: existe en memoria hasta
     * que sale por correo y no queda escrito en ninguna parte.
     */
    public void registerPasswordResetRequestedEvent(Email email, String secret) {
        this.registerEvent(new PasswordResetRequestedEvent(email.email(), secret, this.expiresAt));
    }

    @Override
    public PasswordResetTokenId getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
