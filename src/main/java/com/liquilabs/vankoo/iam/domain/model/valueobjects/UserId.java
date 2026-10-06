package com.liquilabs.vankoo.iam.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Value Object para el ID de Usuario, utilizando UUID para garantizar unicidad.
 * Implementa Serializable para ser utilizado como clave primaria embebida en la entidad User.
 * @param id UUID que representa el ID del usuario, generado automáticamente si no se proporciona.
 */
@Embeddable
public record UserId (
        @Column(columnDefinition = "UUID", nullable = false, unique = true) // Esto le dice a Postgres que use su tipo nativo
        UUID id
) implements Serializable {
    public UserId {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    /**
     * Constructor que genera un nuevo UUIDv7 utilizando el Generador de UUID basado en tiempo de FasterXML, lo que garantiza unicidad y ordenamiento temporal.
     * Esto es especialmente útil para sistemas distribuidos donde se generan IDs en diferentes nodos, evitando colisiones y mejorando el rendimiento en la base de datos.
     */
    public UserId() {
        this(Generators.timeBasedEpochGenerator().generate());
    }
}
