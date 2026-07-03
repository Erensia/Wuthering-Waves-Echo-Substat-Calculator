package com.wuwa.echograder.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SessionUserAuthenticationFilter sessionUserAuthenticationFilter() {
        return new SessionUserAuthenticationFilter();
    }

    @Bean
    RequestRateLimitFilter requestRateLimitFilter() {
        return new RequestRateLimitFilter();
    }

    @Bean
    FilterRegistrationBean<SessionUserAuthenticationFilter> sessionUserFilterRegistration(
            SessionUserAuthenticationFilter filter) {
        FilterRegistrationBean<SessionUserAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<RequestRateLimitFilter> rateLimitFilterRegistration(
            RequestRateLimitFilter filter) {
        FilterRegistrationBean<RequestRateLimitFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SessionUserAuthenticationFilter sessionUserAuthenticationFilter,
            RequestRateLimitFilter requestRateLimitFilter) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/api/v1/csrf",
                                "/api/v1/dashboard/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/scores/calculate").permitAll()
                        .requestMatchers("/api/v1/auth/**", "/api/v1/loadouts/**", "/api/v1/users/**")
                                .authenticated()
                        .anyRequest().denyAll())
                .csrf(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; "
                                        + "script-src 'self'; "
                                        + "style-src 'self'; "
                                        + "img-src 'self' data:; "
                                        + "object-src 'none'; "
                                        + "base-uri 'self'; "
                                        + "frame-ancestors 'none'; "
                                        + "form-action 'self'"))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));

        http.addFilterBefore(sessionUserAuthenticationFilter, AnonymousAuthenticationFilter.class);
        http.addFilterBefore(requestRateLimitFilter, SessionUserAuthenticationFilter.class);
        return http.build();
    }
}
