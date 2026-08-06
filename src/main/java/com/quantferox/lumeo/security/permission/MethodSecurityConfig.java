package com.quantferox.lumeo.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Registers LumeoPermissionEvaluator with Spring Security's method-level
 * expression handler so that hasPermission() works in @PreAuthorize.
 *
 * Spring Security 6 / Spring Boot 3 approach:
 * expose MethodSecurityExpressionHandler as a @Bean.
 * GlobalMethodSecurityConfiguration is legacy (Spring Security 5).
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class MethodSecurityConfig {

    private final LumeoPermissionEvaluator permissionEvaluator;

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler =
                new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
