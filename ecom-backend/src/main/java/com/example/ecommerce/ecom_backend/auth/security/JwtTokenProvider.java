package com.example.ecommerce.ecom_backend.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        // Extract roles (authorities) from the Authentication object
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // Get the string representation of each authority (e.g., "ROLE_USER")
                .collect(Collectors.toList());

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + EXPIRATION_TIME);

        String token = Jwts.builder()
                .setSubject(username)
                .claim("roles", roles) // ADDING THE CUSTOM "roles" CLAIM HERE
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Helper method to extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    // Method to get username (email) from JWT token - NOW USING HELPER
    public String getUsernameFromJwt(String token) {
        return extractClaim(token, Claims::getSubject); // Uses the helper to get the subject claim
    }

    // Method to validate JWT token
    public boolean validateToken(String token) {
        try {
            // Using extractAllClaims will also validate the token (signature, expiration)
            extractAllClaims(token);
            return true;
        } catch (SignatureException ex) {
            System.out.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string is empty");
        }
        return false;
    }
}
