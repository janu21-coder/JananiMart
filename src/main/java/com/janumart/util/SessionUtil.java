package com.janumart.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.janumart.exception.UnauthorizedException;
import com.janumart.exception.ForbiddenException;
import com.janumart.model.User;

/**
 * Session helpers: login state, session-id regeneration, role guards.
 */
public final class SessionUtil {

    public static final String ATTR_USER = "authUser";

    private SessionUtil() {
    }

    public static User getUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object u = session.getAttribute(ATTR_USER);
        return u instanceof User ? (User) u : null;
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getUser(req) != null;
    }

    /** Throws 401 if no authenticated user. */
    public static User requireUser(HttpServletRequest req) {
        User u = getUser(req);
        if (u == null) {
            throw new UnauthorizedException("Please log in to continue.");
        }
        return u;
    }

    /** Throws 401 if not logged in, 403 if role not allowed. */
    public static User requireRole(HttpServletRequest req, String... roles) {
        User u = requireUser(req);
        for (String role : roles) {
            if (u.getRole().equals(role)) {
                return u;
            }
        }
        throw new ForbiddenException("You do not have permission to perform this action.");
    }

    /**
     * Logs the user in: regenerates the session id (session-fixation defence)
     * and stores the authenticated user.
     */
    public static void login(HttpServletRequest req, User user) {
        // Ensure a session exists first, then regenerate its ID (session-fixation defence).
        HttpSession session = req.getSession();
        try {
            req.changeSessionId();
        } catch (IllegalStateException ignored) {
            // Session already committed by the container; proceed with the existing ID.
        }
        session.setAttribute(ATTR_USER, user);
        session.setMaxInactiveInterval(30 * 60);
    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}