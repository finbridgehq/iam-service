package com.liquilabs.vankoo.iam.infrastructure.tokens.secret;

import com.liquilabs.vankoo.iam.application.internal.outboundservices.tokens.SecretTokenService;

/**
 * Marker for the random-secret implementation, mirroring how BearerTokenService sits
 * in front of the JWT one.
 */
public interface RandomSecretTokenService extends SecretTokenService {
}
