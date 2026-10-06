package com.liquilabs.vankoo.iam.application.internal.outboundservices.tokens;

import java.util.List;

public interface TokenService {

    String generateToken(String userId, String email, List<String> roles);

    String getUsernameFromToken(String token);

    boolean validateToken(String token);
}