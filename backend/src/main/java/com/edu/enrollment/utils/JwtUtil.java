package com.edu.enrollment.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(Long userId, String username, String role) {
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("role", role)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .setKey(secret.getBytes())
                .sign();
    }

    public String extractUsername(String token) {
        return (String) parseToken(token).getPayload("sub");
    }

    public Long extractUserId(String token) {
        JWT jwt = parseToken(token);
        Object val = jwt.getPayload("userId");
        if (val instanceof Long) {
            return (Long) val;
        }
        if (val instanceof Integer) {
            return ((Integer) val).longValue();
        }
        return Long.valueOf(val.toString());
    }

    public String extractRole(String token) {
        return (String) parseToken(token).getPayload("role");
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            if (username == null || !username.equals(userDetails.getUsername())) {
                return false;
            }
            return JWTUtil.verify(token, secret.getBytes());
        } catch (Exception e) {
            return false;
        }
    }

    private JWT parseToken(String token) {
        return JWTUtil.parseToken(token);
    }
}