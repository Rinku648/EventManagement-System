package com.eventmanagement.service;

import com.eventmanagement.dao.UserDAO;
import com.eventmanagement.model.User;
import com.eventmanagement.util.PasswordUtil;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String email, String rawPassword) {
        User user = userDAO.findByEmail(email);
        if (user != null && PasswordUtil.matches(rawPassword, user.getPasswordHash())) return user;
        return null;
    }

    public boolean register(User user, String rawPassword) {
        user.setPasswordHash(PasswordUtil.hash(rawPassword));
        return userDAO.createUser(user);
    }
}
