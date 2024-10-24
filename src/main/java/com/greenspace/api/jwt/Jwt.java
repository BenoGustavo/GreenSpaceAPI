package com.greenspace.api.jwt;

import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.greenspace.api.error.http.Unauthorized401Exception;
import com.greenspace.api.models.UserModel;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Utility class for handling JSON Web Tokens (JWT).
 */
@Component
public class Jwt {

    private final Date expirationJwtDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24); // 24 hours

    private final byte[] secretKey;

    public String getCurrentUserJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserModel) {
            // UserModel user = (UserModel) authentication.getPrincipal();
            return (String) authentication.getCredentials();
        }
        return null;
    }

    /**
     * Constructor that initializes the secret key from the environment properties.
     *
     * @param env the environment properties
     * @throws RuntimeException if the JWT_SECRET_KEY is not set
     */
    @Autowired
    public Jwt(Environment env) {
        String secretKeyString = env.getProperty("JWT_SECRET_KEY");

        if (secretKeyString == null) {
            throw new RuntimeException("JWT_SECRET_KEY is not set");
        }

        this.secretKey = Base64.getDecoder().decode(secretKeyString);
    }

    /**
     * Validates the given JWT token against the provided user details.
     *
     * @param token       the JWT token
     * @param userDetails the user details to validate against
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Generates a JWT token for the given authentication.
     *
     * @param authentication the authentication object
     * @return the generated JWT token
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expirationJwtDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Generates a JWT token for the given user entity.
     *
     * @param userModel the user entity object
     * @return the generated JWT token
     */
    public String generateToken(UserModel userModel) {
        return Jwts.builder()
                .setSubject(userModel.getEmailAddress())
                .setIssuedAt(new Date())
                .setExpiration(expirationJwtDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Extracts the claim from the given JWT token.
     *
     * @param token  the JWT token
     * @param claims the claims to extract
     * @return the claim extracted from the token
     */
    public Date extractExpirationDate(String token) {
        return extractClaims(token).getExpiration();
    }

    /**
     * Generates a JWT token for the given user details.
     *
     * @param userDetails the user details object
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        String username = userDetails.getUsername();

        System.out.println("\n\nGenerating token for user: " + username + "\n\n");

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the claims from the given JWT token.
     *
     * @param token the JWT token
     * @return the claims extracted from the token
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the given JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String getCurrentUserEmail() throws Unauthorized401Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            System.out.println("\n\nAuthentication: " + authentication.getName() + "\n\n");
            return authentication.getName();
        }

        throw new Unauthorized401Exception("User is not authenticated");
    }

    public boolean isUserAuthenticated(Authentication authentication) {
        System.out.println("\n\nAuthentication: " + authentication + "\n\n");

        if (authentication instanceof UsernamePasswordAuthenticationToken token) {
            boolean isAuthenticated = token.isAuthenticated();
            return isAuthenticated;
        }

        return false;
    }

    /**
     * Validates the given JWT token against the provided username.
     *
     * @param token    the JWT token
     * @param username the username to validate against
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token, String username) {
        return (username.equals(extractEmail(token)) && !isTokenExpired(token));
    }
}
