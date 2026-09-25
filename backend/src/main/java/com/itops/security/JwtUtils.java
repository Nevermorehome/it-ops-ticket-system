package com.itops.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

/**
 * JWT 工具(HS512)
 */
@Slf4j
@Component
public class JwtUtils {

    /** HS512 要求密钥至少 512 bit = 64 字节 */
    private static final int MIN_SECRET_BYTES = 64;

    /** 仓库内置的开发密钥, 生产环境禁止使用 */
    private static final String DEV_DEFAULT_SECRET =
            "itops-internal-workorder-jwt-secret-key-2026-must-be-long-enough-0123456789";

    private final Environment environment;

    @Value("${itops.jwt.secret:}")
    private String secret;

    @Value("${itops.jwt.expire-minutes:720}")
    private long expireMinutes;

    private SecretKey key;

    public JwtUtils(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT 密钥未配置: 请通过环境变量 ITOPS_JWT_SECRET 设置至少 "
                    + MIN_SECRET_BYTES + " 字节的随机密钥");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT 密钥长度不足: 当前 " + keyBytes.length
                    + " 字节, HS512 至少需要 " + MIN_SECRET_BYTES + " 字节");
        }
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")
                && DEV_DEFAULT_SECRET.equals(secret)) {
            throw new IllegalStateException("生产环境(prod)禁止使用内置开发 JWT 密钥, 请通过环境变量 ITOPS_JWT_SECRET 设置独立随机密钥");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMinutes * 60_000L);
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        return parse(token).getSubject();
    }
}
