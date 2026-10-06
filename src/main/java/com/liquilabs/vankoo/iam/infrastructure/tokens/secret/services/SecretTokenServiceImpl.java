package com.liquilabs.vankoo.iam.infrastructure.tokens.secret.services;

import com.liquilabs.vankoo.iam.infrastructure.tokens.secret.RandomSecretTokenService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Secrets from SecureRandom, digests from SHA-256.
 *
 * <b>Not a UUID.</b> Both ids in this service come from
 * {@code Generators.timeBasedEpochGenerator()}, and copying that here would be the
 * easy mistake to make: a UUIDv7 is ordered in time, so anyone who knows roughly when
 * a reset was requested has most of the value already. A row id may be predictable; a
 * secret may not.
 *
 * 32 bytes because that is the width SHA-256 answers in, and there is nothing to gain
 * from a secret narrower than its own digest. The generator is one long-lived
 * instance: a new SecureRandom per call re-seeds from the OS every time for no
 * benefit.
 */
@Service
public class SecretTokenServiceImpl implements RandomSecretTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SECRET_BYTES = 32;
    private static final String DIGEST_ALGORITHM = "SHA-256";

    @Override
    public String generateSecret() {
        var bytes = new byte[SECRET_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        // URL-safe and unpadded: this string ends up in a query parameter.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String digestOf(String secret) {
        try {
            var digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(secret.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            // Every JVM ships SHA-256. If this throws, the platform is broken in a way
            // no fallback would fix.
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
