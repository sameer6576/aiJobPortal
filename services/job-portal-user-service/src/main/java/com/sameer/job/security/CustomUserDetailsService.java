package com.sameer.job.security;

import com.sameer.job.modal.User;
import com.sameer.job.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);

        if(user==null){
            throw new UsernameNotFoundException("User not found with username: "+username);
        }

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().toString());

        Collection<GrantedAuthority> grantedAuthorities = Collections.singletonList(authority);

        return new org.springframework.security.core.userdetails.User
                (user.getEmail(), user.getPassword(), grantedAuthorities);
    }
}
