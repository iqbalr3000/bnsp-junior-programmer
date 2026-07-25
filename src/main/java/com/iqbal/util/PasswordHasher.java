package com.iqbal.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public static boolean matches(String plain, String hash) {
        return BCrypt.checkpw(plain, hash);
    }
}
