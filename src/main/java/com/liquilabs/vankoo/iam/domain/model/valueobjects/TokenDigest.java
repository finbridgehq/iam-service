package com.liquilabs.vankoo.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * La huella de un secreto de recuperación, nunca el secreto.
 * <p>
 * Lo que se guarda es el SHA-256 en hexadecimal del valor que viajó por correo. Si
 * alguien se lleva un volcado de la base de datos no se lleva ni un solo enlace
 * utilizable: para eso tendría que invertir el hash de 256 bits de entropía.
 * <p>
 * Es SHA-256 y no bcrypt, teniendo bcrypt ya montado, por dos razones. Bcrypt lleva
 * sal, así que su salida no es determinista: no se puede indexar ni buscar por ella,
 * y validar un enlace obligaría a recorrer la tabla entera comparando fila por fila.
 * Y su lentitud existe para proteger secretos que elige una persona; un valor de
 * SecureRandom no tiene nada que ralentizar.
 */
@Embeddable
public record TokenDigest(
        @Column(name = "token_digest", nullable = false, unique = true, length = 64)
        String tokenDigest
) {
    public TokenDigest() {
        this("");
    }

    public TokenDigest {
        if (tokenDigest == null || tokenDigest.isBlank())
            throw new IllegalArgumentException("Token digest cannot be null or blank");
    }
}
