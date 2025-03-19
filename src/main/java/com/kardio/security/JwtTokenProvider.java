package com.kardio.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider for JWT token operations. Handles token generation, validation, and
 * parsing.
 */
@Component
@Slf4j
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private long jwtExpiration;

	@Value("${jwt.issuer:kardio-api}")
	private String jwtIssuer;

	private static final String AUTHORITIES_KEY = "roles";

	/**
	 * Generates a JWT token for the given user details.
	 *
	 * @param userDetails The user details
	 * @return A JWT token
	 */
	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	/**
	 * Generates a JWT token for the given authentication.
	 *
	 * @param authentication The authentication object
	 * @return A JWT token
	 */
	public String generateToken(Authentication authentication) {
		final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		// Extract authorities as a comma-separated string
		final String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		// Add authorities to claims
		final Map<String, Object> claims = new HashMap<>();
		claims.put(AUTHORITIES_KEY, authorities);

		return generateToken(claims, userDetails);
	}

	/**
	 * Generates a JWT token with custom claims for the given user details.
	 *
	 * @param extraClaims Additional claims to include in the token
	 * @param userDetails The user details
	 * @return A JWT token
	 */
	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		final Date now = new Date();
		final Date expiryDate = new Date(now.getTime() + jwtExpiration);

		log.debug("Generating JWT token for user: {}", userDetails.getUsername());

		return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername()).setIssuedAt(now)
				.setExpiration(expiryDate).setIssuer(jwtIssuer).signWith(getSigningKey(), SignatureAlgorithm.HS512)
				.compact();
	}

	/**
	 * Extracts the username from a JWT token.
	 *
	 * @param token The JWT token
	 * @return The username
	 */
	public String getUsernameFromToken(String token) {
		return getAllClaimsFromToken(token).getSubject();
	}

	/**
	 * Extracts the authorities from a JWT token.
	 *
	 * @param token The JWT token
	 * @return The authorities as a comma-separated string
	 */
	public String getAuthoritiesFromToken(String token) {
		final Claims claims = getAllClaimsFromToken(token);
		return claims.get(AUTHORITIES_KEY, String.class);
	}

	/**
	 * Validates a JWT token.
	 *
	 * @param token The JWT token to validate
	 * @return true if the token is valid, false otherwise
	 */
	public boolean validateToken(String token) {
		try {
			// Parse the token and verify signature
			final Claims claims = getAllClaimsFromToken(token);

			// Check expiration
			return !claims.getExpiration().before(new Date());

		} catch (JwtException | IllegalArgumentException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
			throw new JwtException("Invalid JWT token");
		}
	}

	/**
	 * Extracts all claims from a JWT token.
	 *
	 * @param token The JWT token
	 * @return The claims
	 */
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
	}

	/**
	 * Gets the signing key for JWT token generation and validation.
	 *
	 * @return The signing key
	 */
	private SecretKey getSigningKey() {
		final byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}