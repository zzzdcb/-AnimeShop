package com.dong.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtUtils {

    private String secret;
    private Long expirationTime;

    // 获取签名密钥
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成Token
     *
     * @param claims 数据声明
     * @return Token
     */
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .claims(claims)                    // 新版
                .issuedAt(now)                     // 新版
                .expiration(expiryDate)            // 新版
                .signWith(getSigningKey(), Jwts.SIG.HS256)  // 新版
                .compact();
    }

    /**
     * 解析Token
     *
     * @param token Token
     * @return 数据声明
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();  // 注意：getPayload() 返回 Claims
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期，请重新登录", e);
        } catch (JwtException e) {
            throw new RuntimeException("Token无效: " + e.getMessage(), e);
        }
    }

    /**
     * 验证Token
     *
     * @param token Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);  // 假设存的时候 key 是 "userId"
    }
}