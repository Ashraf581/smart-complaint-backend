package com.smartcomplaints.complaint.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ✅ 1. Public first — login/register
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // ✅ 2. Citizens + Admins can submit
                        .requestMatchers(HttpMethod.POST,
                                "/api/complaint")
                        .authenticated()

                        // ✅ 3. Citizens + Admins can view
                        .requestMatchers(HttpMethod.GET,
                                "/api/complaints")
                        .authenticated()

                        // ✅ 4. Admin only — update status
                        .requestMatchers(HttpMethod.PUT,
                                "/api/complaint/*/status")
                        .hasRole("ADMIN")

                        // ✅ 5. Admin only — reclassify
                        .requestMatchers(HttpMethod.PUT,
                                "/api/complaint/*/reclassify")
                        .hasRole("ADMIN")

                        // ✅ 6. Admin only — delete
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/complaint/*")
                        .hasRole("ADMIN")

                        // ✅ 7. Everything else — must login
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow ALL origins for now
        // (we can restrict later)
        config.setAllowedOriginPatterns(
                List.of("*"));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT",
                        "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
