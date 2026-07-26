package com.rooms.auth_service.service;

import com.rooms.auth_service.entity.User;
import com.rooms.auth_service.repository.UserRepository;
import com.rooms.auth_service.entity.OauthAccount;
import com.rooms.auth_service.repository.OauthAccountRepository;
import com.rooms.auth_service.security.CustomUserDetailsService;
import com.rooms.auth_service.security.UserPrincipal;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import com.rooms.auth_service.exception.BadRequestException;
import com.rooms.auth_service.exception.ResourceNotFoundException;
import com.rooms.auth_service.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OauthAccountRepository oauthAccountRepository;
    private final CustomUserDetailsService userDetailsService;
    private final Argon2 argon2;
    private final String pepper;

    public UserService(UserRepository userRepository, 
                       OauthAccountRepository oauthAccountRepository, 
                       CustomUserDetailsService userDetailsService,
                       @Value("${app.password.pepper}") String pepper) {
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.userDetailsService = userDetailsService;
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        this.pepper = pepper;
    }

    public User createUserWithEmail(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Email already in use");
        }
        String hash = argon2.hash(10, 65536, 1, (password + pepper).toCharArray());
        User user = User.builder()
                .email(email)
                .passwordHash(hash)
                .build();
        return userRepository.save(user);
    }

    public User setUsername(UUID userId, String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BadRequestException("Username already taken");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setUsername(username);
        return userRepository.save(user);
    }

    public User authenticate(String emailOrUsername, String password) {
        try {
            UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(emailOrUsername);
            User user = principal.getUser();
            
            if (user.getPasswordHash() == null) {
                 throw new UnauthorizedException("Invalid credentials");
            }
            boolean matches = argon2.verify(user.getPasswordHash(), (password + pepper).toCharArray());
            if (!matches) {
                throw new UnauthorizedException("Invalid credentials");
            }
            return user;
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void updatePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getPasswordHash() == null) {
            throw new BadRequestException("User does not have a password set. Log in with OAuth.");
        }
        if (!argon2.verify(user.getPasswordHash(), (oldPassword + pepper).toCharArray())) {
            throw new UnauthorizedException("Incorrect old password");
        }
        String newHash = argon2.hash(10, 65536, 1, (newPassword + pepper).toCharArray());
        user.setPasswordHash(newHash);
        userRepository.save(user);
    }

    public User handleOAuthLogin(String email, String googleUserId) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder().email(email).build();
            return userRepository.save(newUser);
        });

        oauthAccountRepository.findByProviderAndProviderUserId("google", googleUserId)
            .orElseGet(() -> {
                OauthAccount account = OauthAccount.builder()
                        .user(user)
                        .provider("google")
                        .providerUserId(googleUserId)
                        .email(email)
                        .build();
                return oauthAccountRepository.save(account);
            });

        return user;
    }
}
