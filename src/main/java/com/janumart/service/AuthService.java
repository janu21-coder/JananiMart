package com.janumart.service;

import com.janumart.dao.UserDao;
import com.janumart.dto.LoginRequest;
import com.janumart.dto.RegisterRequest;
import com.janumart.exception.UnauthorizedException;
import com.janumart.exception.ValidationException;
import com.janumart.model.User;
import com.janumart.util.PasswordUtil;
import com.janumart.util.Validate;

/**
 * Registration, login and profile management. ADMIN can never be selected
 * during registration.
 */
public class AuthService {

    private final UserDao userDao = new UserDao();

    public User register(RegisterRequest req) {
        String name = Validate.requireText(req.getName(), "Name", 100);
        String email = Validate.requireText(req.getEmail(), "Email", 150);
        Validate.email(email);
        String password = Validate.requireText(req.getPassword(), "Password", 72);
        Validate.password(password);
        Validate.confirm(password, req.getConfirmPassword());

        String role = req.getRole() == null ? "BUYER" : req.getRole().trim().toUpperCase();
        if (!role.equals("BUYER") && !role.equals("SELLER")) {
            throw new ValidationException("Invalid account type. Admin registration is not allowed.");
        }
        if (userDao.findByEmail(email) != null) {
            throw new ValidationException("An account with this email already exists.");
        }

        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash(password));
        u.setRole(role);
        userDao.insert(u);
        return u;
    }

    public User login(LoginRequest req) {
        String email = Validate.requireText(req.getEmail(), "Email", 150);
        String password = Validate.requireText(req.getPassword(), "Password", 72);
        Validate.email(email);

        User u = userDao.findByEmail(email);
        if (u == null || !PasswordUtil.matches(password, u.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        return u;
    }

    public void updateProfile(int userId, String newName) {
        String name = Validate.requireText(newName, "Name", 100);
        userDao.updateName(userId, name);
    }

    public void changePassword(int userId, String current, String next, String confirm) {
        User u = userDao.findById(userId);
        if (u == null) {
            throw new UnauthorizedException("Account not found.");
        }
        String currentPassword = Validate.requireText(current, "Current password", 72);
        if (!PasswordUtil.matches(currentPassword, u.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }
        String newPassword = Validate.requireText(next, "New password", 72);
        Validate.password(newPassword);
        Validate.confirm(newPassword, confirm);
        userDao.updatePassword(userId, PasswordUtil.hash(newPassword));
    }
}