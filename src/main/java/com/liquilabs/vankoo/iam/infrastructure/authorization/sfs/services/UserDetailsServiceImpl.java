package com.liquilabs.vankoo.iam.infrastructure.authorization.sfs.services;

import com.liquilabs.vankoo.iam.domain.model.valueobjects.UserId;
import com.liquilabs.vankoo.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.liquilabs.vankoo.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service(value = "defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userId = new UserId(UUID.fromString(username));
        var user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + username));
        return UserDetailsImpl.build(user);
    }
}