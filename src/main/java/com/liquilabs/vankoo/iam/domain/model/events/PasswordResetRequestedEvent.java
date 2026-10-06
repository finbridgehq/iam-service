package com.liquilabs.vankoo.iam.domain.model.events;

import java.util.Date;

/**
 * Alguien pidió recuperar su contraseña y hay un enlace que entregarle.
 * <p>
 * <b>Este evento no sale nunca a Kafka.</b> Lleva el secreto en claro, que es lo único
 * que abre esa puerta; su único consumidor legítimo es el manejador que se lo entrega
 * al puerto de correo, y de ahí no pasa. Publicarlo en un topic lo dejaría escrito en
 * el broker, en sus réplicas y en cualquier consumidor que se suscriba mañana.
 */
public record PasswordResetRequestedEvent(
        String email,
        String token,
        Date expiresAt
) {
}
