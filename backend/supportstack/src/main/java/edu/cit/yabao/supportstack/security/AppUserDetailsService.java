package edu.cit.yabao.supportstack.security;

import edu.cit.yabao.supportstack.model.User;
import edu.cit.yabao.supportstack.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String principal = user.getUsername() != null ? user.getUsername() : user.getEmail();

        return org.springframework.security.core.userdetails.User.builder()
                .username(principal)
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}
