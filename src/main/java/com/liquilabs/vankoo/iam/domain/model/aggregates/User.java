package com.liquilabs.vankoo.iam.domain.model.aggregates;

import com.liquilabs.vankoo.iam.domain.model.entities.Role;
import com.liquilabs.vankoo.iam.domain.model.events.PasswordChangedEvent;
import com.liquilabs.vankoo.iam.domain.model.events.UserCreatedEvent;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Email;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Password;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.UserId;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extiende de AbstractAggregateRoot para poder registrar eventos de dominio relacionados con esta entidad.
 * Implementa Persistable para controlar el estado de la entidad (nueva o existente) y optimizar las operaciones de persistencia en Spring Data JPA.
 * Utiliza @EntityListeners para habilitar el soporte de auditoría automática de Spring Data JPA, lo que permite que los campos.
 * <p>
 * Básicamente, no necesita heredar de una clase `AuditableAbstractAggregateRoot` para evitar acoplamiento.
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true) // Índice único para el campo email
})
public class User extends AbstractAggregateRoot<User> implements Persistable<UserId> {

    /**
     * Utilizamos @EmbeddedId para indicar que el campo id es una clave primaria compuesta y está embebida en la entidad User.
     * Esto es necesario porque UserId es un value object que representa la identidad de la entidad User, y queremos que su valor se utilice como clave primaria en la base de datos.
     * Usa UUID.
     */
    @EmbeddedId
    private UserId id;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @ManyToMany(fetch = FetchType.LAZY) // Carga perezosa para evitar el problema de N+1 queries al cargar un usuario sin necesidad de sus roles
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

    protected User() {}

    public User(Email email, Password password) {
        this.id = new UserId();
        this.email = email;
        this.password = password;
        this.roles = new HashSet<>();
    }

    public User(Email email, Password password, List<Role> roles) {
        this(email, password);
        addRoles(roles);
    }

    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    public User addRoles(List<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
        return this;
    }

    /**
     * Cambia la contraseña.
     * <p>
     * Recibe el valor ya cifrado, igual que el constructor: el agregado nunca ve la
     * contraseña en claro, y quien la cifra es el mismo servicio de aplicación que ya
     * lo hace al registrarse.
     */
    public void changePassword(Password password) {
        this.password = password;
    }

    public void registerPasswordChangedEvent() {
        this.registerEvent(
                new PasswordChangedEvent(this.id.id().toString(), this.getEmail().email()));
    }

    public void registerUserCreatedEvent() {
        this.registerEvent(
                new UserCreatedEvent(
                        this.id.id().toString(),
                        this.getEmail().email(),
                        this.getRoles().stream().map(role -> role.getName().name()).toList()));
    }

    /**
     * Devuelve el ID de la entidad, que en este caso es un UserId.
     * Esto es requerido por la interfaz Persistable para identificar la entidad.
     */
    @Override
    public UserId getId() {
        return id;
    }

    /**
     * Indica si la entidad es nueva (no persistida) o ya existe en la base de datos.
     * Viene de la interfaz Persistable que implementa esta clase líneas arriba.
     * Es utilizada por Spring Data JPA para determinar el estado de la entidad.
     * Esto es necesario para que Spring Data JPA sepa si debe realizar un INSERT o un UPDATE.
     * En este caso, consideramos que una entidad es nueva si su campo createdAt es null, lo que significa que aún no ha sido persistida.
     * Mejora el rendimiento al evitar consultas adicionales para verificar la existencia de la entidad en la base de datos.
     */
    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
