package com.example.stage.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class LdapPasswordEncoder {

    public static String encodeSSHA(String password) {
        byte[] salt = new byte[8];
        new SecureRandom().nextBytes(salt);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            digest.update(salt);
            byte[] hash = digest.digest();

            byte[] hashPlusSalt = new byte[hash.length + salt.length];
            System.arraycopy(hash, 0, hashPlusSalt, 0, hash.length);
            System.arraycopy(salt, 0, hashPlusSalt, hash.length, salt.length);

            return "{SSHA}" + Base64.getEncoder().encodeToString(hashPlusSalt);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}