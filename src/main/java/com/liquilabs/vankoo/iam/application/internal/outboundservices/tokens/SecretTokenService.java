package com.liquilabs.vankoo.iam.application.internal.outboundservices.tokens;

/**
 * Opaque one-time secrets, and the digest the database keeps instead.
 *
 * Separate from {@link TokenService}, which mints JWTs: those are signed, readable
 * and carry claims, while these are meaningless strings whose only property is being
 * impossible to guess. Sharing one port would invite someone to reach for the wrong
 * one.
 */
public interface SecretTokenService {

    /** A fresh secret. It is shown to its owner once and never stored as it is. */
    String generateSecret();

    /** The digest a secret is looked up by. Deterministic, so it can be indexed. */
    String digestOf(String secret);
}
