package com.hst.materialmgmt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:PandaAquaSecretKey2026!MustBe32CharLong!!}")
    private String secret;

    @Value("${jwt.expiry-hours:12}")
    private long expiryHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // companyId added — required for tenant isolation on every subsequent request
    public String generate(String username, String role, String fullName, String companyId) {
        return Jwts.builder()
            .setSubject(username)
            .claim("role",      role)
            .claim("fullName",  fullName)
            .claim("companyId", companyId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiryHours * 3600_000L))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key()).build()
            .parseClaimsJws(token).getBody();
    }

    public boolean isValid(String token) {
        try { parse(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public String getUsername(String token)  { return parse(token).getSubject(); }
    public String getRole(String token)      { return (String) parse(token).get("role"); }
    public String getCompanyId(String token) { return (String) parse(token).get("companyId"); }
}