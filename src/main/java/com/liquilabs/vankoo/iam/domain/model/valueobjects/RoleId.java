package com.liquilabs.vankoo.iam.domain.model.valueobjects;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record RoleId(
        @Column(columnDefinition = "UUID", nullable = false, unique = true)
        UUID id
) implements Serializable {
    public RoleId {
        if (id == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
    }

    public RoleId() {
        this((Generators.timeBasedEpochGenerator().generate()));
    }
}
