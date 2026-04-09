package com.websites.coffeeshop.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// Password generator for manually creating encrypted passwords
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // System.out.println("Encrypted password: " + encodedPassword);
    }
}

