package org.paytrack.com.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Customer Create: Owner, Finance manager, Accountant
                .requestMatchers(HttpMethod.POST, "/api/v1/customers").hasAnyRole("OWNER", "FINANCE_MANAGER", "ACCOUNTANT")
                // Customer Update: Owner, Finance manager, Accountant
                .requestMatchers(HttpMethod.PUT, "/api/v1/customers/**").hasAnyRole("OWNER", "FINANCE_MANAGER", "ACCOUNTANT")
                // Customer Delete: Owner, Finance manager only
                .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/**").hasAnyRole("OWNER", "FINANCE_MANAGER")
                // Customer Read: All 5 roles
                .requestMatchers(HttpMethod.GET, "/api/v1/customers/**").hasAnyRole("OWNER", "FINANCE_MANAGER", "ACCOUNTANT", "COLLECTOR", "READ_ONLY")
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails owner = User.builder()
            .username("owner")
            .password(passwordEncoder.encode("owner123"))
            .roles("OWNER")
            .build();

        UserDetails finance = User.builder()
            .username("finance")
            .password(passwordEncoder.encode("finance123"))
            .roles("FINANCE_MANAGER")
            .build();

        UserDetails accountant = User.builder()
            .username("accountant")
            .password(passwordEncoder.encode("accountant123"))
            .roles("ACCOUNTANT")
            .build();

        UserDetails collector = User.builder()
            .username("collector")
            .password(passwordEncoder.encode("collector123"))
            .roles("COLLECTOR")
            .build();

        UserDetails readonly = User.builder()
            .username("readonly")
            .password(passwordEncoder.encode("readonly123"))
            .roles("READ_ONLY")
            .build();

        return new InMemoryUserDetailsManager(owner, finance, accountant, collector, readonly);
    }
}
