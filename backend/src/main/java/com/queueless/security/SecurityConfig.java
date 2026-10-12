package com.queueless.security;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// This class decides WHO can open WHICH url (role-based access control).
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // BCrypt turns "abc123" into a long scrambled text. It cannot be turned back.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())                 // allow our React app to call us
            .csrf(csrf -> csrf.disable())                    // not needed, we use JWT (no cookies)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/test", "/api/auth/**", "/error").permitAll()   // anyone
                .requestMatchers("/api/admin/**").hasRole("ADMIN")          // admin only
                .requestMatchers("/api/staff/**").hasRole("STAFF")          // staff only
                .requestMatchers("/api/queue/**").hasRole("STUDENT")        // students only
                .anyRequest().authenticated())                              // everything else: any logged-in user
            // when someone is not logged in, answer 401 (React will send them to the login page)
            .exceptionHandling(e -> e.authenticationEntryPoint(
                (request, response, ex) -> response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
            // run our JWT filter before Spring's normal login filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS: the browser blocks calls from localhost:3000 (React) to localhost:8080 (backend)
    // unless the backend says "I allow it". This is that permission.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
