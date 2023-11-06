package com.exam.config;


import com.exam.services.TeacherDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    private final TeacherDetailsService userDetailsService;

    public SecurityConfiguration(TeacherDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }



    @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http

            .httpBasic(Customizer.withDefaults())
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests((authorizeHttpRequests) ->
                    authorizeHttpRequests
                            .requestMatchers(
                "/exam/**",
                "/uploads/**",
                "/static/images/**",
                 "/app/uploads/**"
        )
          .permitAll()
          .anyRequest()
          .authenticated())

        .csrf(CsrfConfigurer::disable)
        .formLogin((form) -> form
                .loginPage("/login")
                .permitAll()
        )
        .logout(LogoutConfigurer::permitAll)
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }

    @Bean
    public PasswordEncoder passwordEncoder() {
        final int strength = 10;
        return new BCryptPasswordEncoder(strength);
    }

    @Lazy
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder()); // Call the method to get the bean

        return auth.build();
    }
}
