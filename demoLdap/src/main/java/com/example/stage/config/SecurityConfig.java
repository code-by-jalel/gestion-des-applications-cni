package com.example.stage.config;

import com.example.stage.security.CustomLdapAuthoritiesPopulator;
import com.example.stage.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/api/auth/login").permitAll()
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers("/api/users/**").hasAnyAuthority("ROLE_ADMINSGROUP","ROLE_GESTIONNAIREUTILISATEURS")
                        .requestMatchers("/api/groups/**").hasAnyAuthority("ROLE_ADMINSGROUP","ROLE_GESTIONNAIREUTILISATEURS")
                        .requestMatchers("/api/organisation/**").hasAnyAuthority("ROLE_ADMINSGROUP","ROLE_GESTIONNAIREUTILISATEURS")
                        .requestMatchers("/api/dashboard/**").hasAuthority("ROLE_ADMINSGROUP")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    LdapAuthoritiesPopulator authoritiesPopulator(BaseLdapPathContextSource contextSource,
                                                  LdapTemplate ldapTemplate) {
        CustomLdapAuthoritiesPopulator populator =
                new CustomLdapAuthoritiesPopulator(contextSource, "ou=groups", ldapTemplate);
        populator.setGroupSearchFilter("member={0}");
        populator.setRolePrefix("ROLE_");
        populator.setConvertToUpperCase(true);
        return populator;
    }

    @Bean
    AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource,
                                                LdapAuthoritiesPopulator authoritiesPopulator) {
        LdapBindAuthenticationManagerFactory factory =
                new LdapBindAuthenticationManagerFactory(contextSource);

        factory.setUserSearchFilter("mail={0}");
        factory.setUserSearchBase("ou=users");

        factory.setLdapAuthoritiesPopulator(authoritiesPopulator);
        return factory.createAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
