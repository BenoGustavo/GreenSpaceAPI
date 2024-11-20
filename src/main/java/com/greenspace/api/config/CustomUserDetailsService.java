package com.greenspace.api.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.models.PermissionModel;
import com.greenspace.api.models.UserModel;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                UserModel user = userRepository.findByEmailAddress(email).orElseThrow(
                                () -> new NotFound404Exception(
                                                "Error creating userdetails in java spring user not found: " + email));

                PermissionModel permissionLevel = user.getPermissionLevel();

                // Convert roles to GrantedAuthority
                Set<GrantedAuthority> authorities = new HashSet<>(
                                Set.of(new SimpleGrantedAuthority(permissionLevel.getName().toString())));

                return new User(user.getEmailAddress(), user.getPassword(),
                                authorities);
        }
}
