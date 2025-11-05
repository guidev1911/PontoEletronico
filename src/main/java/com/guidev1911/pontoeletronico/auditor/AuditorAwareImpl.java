package com.guidev1911.pontoeletronico.auditor;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.of("system");
        }

        Object principal = auth.getPrincipal();
        String username = (principal instanceof org.springframework.security.core.userdetails.User userDetails)
                ? userDetails.getUsername()
                : auth.getName();

        return Optional.ofNullable(username != null ? username : "system");
    }
}