package com.projekt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/knowledge-base/**", "/software-list/**", "/webjars/**", "/", "/register", "/knowledge-search", "/software-search", "/activate/**").permitAll()
                .antMatchers("/knowledge/**", "/software/**", "/category/**", "/priority/**", "/status/**", "/user/**", "/user-search").hasRole("ADMIN")
                .antMatchers("/category-list/", "/priority-list/", "/status-list/", "/tickets/**", "/ticket-search").hasRole("OPERATOR")
                .antMatchers("/my-tickets","/ticket/**","/profile/**").hasRole("USER")

                .anyRequest().authenticated();

        http
                .formLogin()
                .loginPage("/login")
                .permitAll();

        http.logout().permitAll();

        http.exceptionHandling().accessDeniedPage("/error403");

        http.csrf()
                .ignoringAntMatchers("/h2-console/**");
        http.headers()
                .frameOptions()
                .sameOrigin();

    }
}
