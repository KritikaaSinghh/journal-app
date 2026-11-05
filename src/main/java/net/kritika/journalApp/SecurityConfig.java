
package net.kritika.journalApp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository
    ) throws Exception {

        http
                .csrf().disable()
                .securityContext()
                .securityContextRepository(securityContextRepository)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeRequests()
                .antMatchers(
                        "/",
                        "/index.html",
                        "/api/auth/**",
                        "/css/**",
                        "/js/**",

                        // Swagger UI
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()
                .antMatchers("/api/journal/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .formLogin().disable()
                .httpBasic().disable();

        return http.build();
    }
}
