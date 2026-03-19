package com.travis.bankingapp.auth;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  // build cryptographic signing key from secret string
  private Key getSigningKey() {
    return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
  }

  // generate JWT for given user email
  public String generateToken(String email) {
    return Jwts.builder()
      .subject(email)
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
      .signWith(getSigningKey())
      .compact();
  }

  // extract user's email (subject) from token
  public String extractEmail(String token) {
    return extractAllClaims(token).getSubject();
  }

  // validates that token belongs to a user and has not expired
  public boolean isTokenValid(String token, String email) {
    String extractedEmail = extractEmail(token);

    return extractedEmail.equals(email) && !isTokenExpired(token);
  }

  // checks whether token's expiration has passed
  private boolean isTokenExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }

  // parses token and returns all claims
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
      .verifyWith((javax.crypto.SecretKey) getSigningKey()) // verify signature
      .build()
      .parseSignedClaims(token)
      .getPayload(); // return payload (claims)
  }
}
