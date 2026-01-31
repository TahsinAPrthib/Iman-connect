package com.faithapp.utils;

import org.mindrot.jbcrypt.BCrypt;

public class SecurityUtils {
    private static final int BCRYPT_ROUNDS = 12;

    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 