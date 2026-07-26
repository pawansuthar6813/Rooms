package com.rooms.auth_service.security;

import com.rooms.auth_service.entity.User;
import com.rooms.auth_service.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(emailOrUsername);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(emailOrUsername);
        }
        
        User user = userOpt.orElseThrow(() -> 
            new UsernameNotFoundException("User not found with email or username: " + emailOrUsername)
        );

        return new UserPrincipal(user);
    }
}
