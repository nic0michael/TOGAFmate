package com.togafmate.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.togafmate.backend.repository.UserRepository;

/** Configures authentication and permission checks for backend endpoints. */
@Configuration
public class SecurityConfiguration {
    /** Builds the password encoder used when saving and checking passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Loads persisted users for HTTP Basic authentication. */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return userID -> userRepository.findForLogon(userID)
                .map(user -> org.springframework.security.core.userdetails.User.withUsername(user.getUserID())
                        .password(user.getPassword())
                        .authorities(user.getPermissions().stream().map(Enum::name).toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));
    }

    /** Applies public, authenticated, and administrator-only access rules. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/logon").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/permissions")
                        .hasAuthority("ADMINISTRATOR_ACCESS")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*/permissions")
                        .hasAuthority("ADMINISTRATOR_ACCESS")
                        .requestMatchers(HttpMethod.GET, "/api/knowledge-bases")
                        .hasAnyAuthority("USER_ACCESS", "ADMINISTRATOR_ACCESS")
                        .requestMatchers(HttpMethod.POST, "/api/knowledge-bases")
                        .hasAuthority("ADMINISTRATOR_ACCESS")
                        .anyRequest().denyAll());
        return http.build();
    }
}
