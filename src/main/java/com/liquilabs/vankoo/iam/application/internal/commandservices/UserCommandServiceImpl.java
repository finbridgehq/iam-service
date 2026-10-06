package com.liquilabs.vankoo.iam.application.internal.commandservices;

import com.liquilabs.vankoo.iam.application.internal.outboundservices.hashing.HashingService;
import com.liquilabs.vankoo.iam.application.internal.outboundservices.tokens.SecretTokenService;
import com.liquilabs.vankoo.iam.application.internal.outboundservices.tokens.TokenService;
import com.liquilabs.vankoo.iam.domain.exceptions.EmailAlreadyInUseException;
import com.liquilabs.vankoo.iam.domain.exceptions.InvalidCredentialsException;
import com.liquilabs.vankoo.iam.domain.exceptions.InvalidPasswordResetTokenException;
import com.liquilabs.vankoo.iam.domain.model.aggregates.PasswordResetToken;
import com.liquilabs.vankoo.iam.domain.model.aggregates.User;
import com.liquilabs.vankoo.iam.domain.model.commands.RequestPasswordResetCommand;
import com.liquilabs.vankoo.iam.domain.model.commands.ResetPasswordCommand;
import com.liquilabs.vankoo.iam.domain.model.commands.SignInCommand;
import com.liquilabs.vankoo.iam.domain.model.commands.SignUpCommand;
import com.liquilabs.vankoo.iam.domain.model.entities.Role;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.Password;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.RoleName;
import com.liquilabs.vankoo.iam.domain.model.valueobjects.TokenDigest;
import com.liquilabs.vankoo.iam.domain.services.UserCommandService;
import com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository;
import com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final SecretTokenService secretTokenService;
    private final int passwordResetExpirationMinutes;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            HashingService hashingService,
            TokenService tokenService,
            SecretTokenService secretTokenService,
            @Value("${authorization.password-reset.expiration.minutes}") int passwordResetExpirationMinutes
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.secretTokenService = secretTokenService;
        this.passwordResetExpirationMinutes = passwordResetExpirationMinutes;
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new EmailAlreadyInUseException(command.email());
        var roleNames = (command.roles() == null || command.roles().isEmpty())
                ? List.of(RoleName.ROLE_USER)
                : command.roles().stream().map(Role::getName).toList();
        var roles = roleRepository.findAllByNameIn(roleNames);
        // Reaching here means the roles table was never seeded, not that the client got
        // anything wrong: the requested names were already checked against the
        // self-assignable set before the command was built.
        if (roles.size() != roleNames.size())
            throw new IllegalStateException("Roles missing from the database: " + roleNames);
        var user = new User(command.email(), new Password(hashingService.encode(command.password().password())), roles);
        user.registerUserCreatedEvent();
        userRepository.save(user);
        return Optional.of(user);
    }

    @Override
    @Transactional
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        // Both failures below raise the same exception on purpose: an unknown address
        // and a wrong password must be impossible to tell apart from outside, or this
        // endpoint becomes a way of discovering who has an account.
        var user = userRepository.findByEmailWithRoles(command.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!hashingService.matches(command.password().password(), user.getPassword().password()))
            throw new InvalidCredentialsException();
        var roles = user.getRoles().stream().map(role -> role.getName().name()).toList();
        var token = tokenService.generateToken(Objects.requireNonNull(user.getId()).id().toString(), user.getEmail().email(), roles);
        return Optional.of(ImmutablePair.of(user, token));
    }

    @Override
    @Transactional
    public void handle(RequestPasswordResetCommand command) {
        // El secreto se genera y se digiere antes de mirar la base de datos, exista la
        // cuenta o no: así el coste criptográfico se paga en los dos caminos y no es
        // lo que distingue un correo registrado de uno que no lo está.
        var secret = secretTokenService.generateSecret();
        var digest = new TokenDigest(secretTokenService.digestOf(secret));
        var user = userRepository.findByEmail(command.email()).orElse(null);
        if (user == null) {
            // Sin la dirección: escribir cuáles no existen dejaría en el log justo la
            // respuesta que el endpoint se niega a dar.
            LOGGER.info("Password reset requested for an address with no account");
            return;
        }
        // Primero borrar, y en un solo statement. Cada cuenta tiene como mucho un
        // enlace vivo; si no, quien pidió uno hace media hora lo conserva funcionando
        // después de que su dueño pida el suyo.
        passwordResetTokenRepository.deleteAllByUserId(user.getId());
        var resetToken = new PasswordResetToken(
                user.getId(),
                digest,
                DateUtils.addMinutes(new Date(), passwordResetExpirationMinutes));
        resetToken.registerPasswordResetRequestedEvent(user.getEmail(), secret);
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    @Transactional
    public void handle(ResetPasswordCommand command) {
        // Las tres formas de fallar —no existe, caducó, ya se usó— levantan la misma
        // excepción, por lo mismo que los dos fallos de inicio de sesión: contarlas
        // convertiría esto en una manera de averiguar qué enlaces han existido.
        var digest = new TokenDigest(secretTokenService.digestOf(command.token()));
        var resetToken = passwordResetTokenRepository.findByTokenDigest(digest)
                .orElseThrow(InvalidPasswordResetTokenException::new);
        var now = new Date();
        if (!resetToken.isUsable(now))
            throw new InvalidPasswordResetTokenException();
        var user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(InvalidPasswordResetTokenException::new);
        user.changePassword(new Password(hashingService.encode(command.password().password())));
        user.registerPasswordChangedEvent();
        // Consumido en la misma transacción que el cambio: si una falla, no pasa la otra.
        resetToken.consume(now);
        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
    }
}
