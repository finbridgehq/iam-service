package com.liquilabs.vankoo.iam.domain.model.events;

/**
 * La contraseña de una cuenta cambió.
 * <p>
 * Sin la contraseña ni el token, claro: solo quién y cuándo, que es lo que otro
 * contexto podría querer saber.
 */
public record PasswordChangedEvent(
        String id,
        String email
) {
}
