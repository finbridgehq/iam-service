package com.liquilabs.vankoo.iam.infrastructure.hashing.bcrypt;

import com.liquilabs.vankoo.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
