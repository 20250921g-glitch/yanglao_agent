package com.care; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 
public class PasswordTest { 
    public static void main(String[] args) { 
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); 
        String hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi"; 
        String[] passwords = {"admin", "admin123", "123456", "password"}; 
        for (String p : passwords) { 
            System.out.println(p + ": " + encoder.matches(p, hash)); 
        } 
    } 
} 
