package com.example.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // 静态密钥：类加载时生成，确保全局唯一且符合HS256长度要求
    private static final SecretKey secretKey;

    // 静态初始化块：生成并校验密钥
    static {
        // 生成HS256算法要求的256位(32字节)密钥
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // 强制校验密钥长度（32字节=256位）
        int keyLength = secretKey.getEncoded().length;
        if (keyLength < 32) {
            log.error("JWT密钥长度不足！要求至少32字节，实际：{}字节", keyLength);
            throw new IllegalStateException("JWT signing key is not secure enough for HS256 algorithm");
        }
        log.info("JWT密钥初始化成功，长度：{}字节（符合HS256要求）", keyLength);
    }

    // 从配置文件读取过期时间（默认24小时）
    @Value("${jwt.expire:86400000}")
    private long expire;

    // 注入Redis模板用于存储token
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成JWT Token
     * @param username 用户名
     * @param role 角色
     * @return 生成的Token字符串
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire);

        try {
            return Jwts.builder()
                    .setSubject(username)
                    .claim("role", role)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(secretKey) // 使用静态密钥签名
                    .compact();
        } catch (Exception e) {
            log.error("生成JWT Token失败", e);
            throw new RuntimeException("Token生成失败", e);
        }
    }

    /**
     * 解析Token获取载荷信息
     * @param token JWT Token字符串
     * @return 包含的Claims（载荷）
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 使用静态密钥解析
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析JWT Token失败", e);
            throw new RuntimeException("Token解析失败", e);
        }
    }

    /**
     * 将Token存储到Redis
     */
    public void saveToken(String username, String token) {
        try {
            redisTemplate.opsForValue().set(
                    "token:" + username,
                    token,
                    expire,
                    TimeUnit.MILLISECONDS
            );
            log.debug("Token已存储到Redis，用户：{}", username);
        } catch (Exception e) {
            log.error("存储Token到Redis失败", e);
            throw new RuntimeException("Token存储失败", e);
        }
    }

    /**
     * 验证Token有效性
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            String username = claims.getSubject();
            String redisToken = (String) redisTemplate.opsForValue().get("token:" + username);

            boolean isExpired = claims.getExpiration().before(new Date());
            boolean isConsistent = token.equals(redisToken);

            if (isExpired) {
                log.warn("Token已过期，用户：{}", username);
                return false;
            }
            if (!isConsistent) {
                log.warn("Token与Redis存储不一致，用户：{}", username);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Token验证失败", e);
            return false;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        return (String) parseToken(token).get("role");
    }
}