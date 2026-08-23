package com.musicapp.controller;

import com.musicapp.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small helper so every controller doesn't repeat the same
 * SecurityContext-unwrapping code to get the logged-in user's ID.
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }
}
