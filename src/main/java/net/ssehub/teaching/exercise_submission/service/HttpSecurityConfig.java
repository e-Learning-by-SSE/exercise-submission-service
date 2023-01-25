package net.ssehub.teaching.exercise_submission.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration for spring security.
 * 
 * @author Adam
 */
@Configuration
@EnableWebSecurity
public class HttpSecurityConfig {

    /**
     * Configures the filter-chain for spring security.
     * 
     * @param http The {@link HttpSecurity} provided by spring security.
     * 
     * @return The filter-chain created via the http parameter.
     * 
     * @throws Exception If the http parameter throws an exception.
     */
    @Bean
    // checkstyle: stop exception type check
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // checkstyle: resume exception type check
        http.authorizeHttpRequests()
            .requestMatchers("/actuator", "/actuator/**").permitAll()
            .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
            .requestMatchers("/**").authenticated();
        
        return http.build();
    }
    
}
