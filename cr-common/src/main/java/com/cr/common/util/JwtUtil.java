package com.cr.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.experimental.UtilityClass;

import java.util.Date;

/**
 * Utility for creating and verifying JWT access tokens.
 */
@UtilityClass
public class JwtUtil {

    /**
     * Fixed secret used to sign tokens.
     */
    private static final String SECRET = "cr_recommend_secret_key_2024";

    /**
     * Token lifetime in milliseconds.
     */
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000L;

    /**
     * HMAC signing algorithm.
     */
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    /**
     * Token verifier.
     */
    private static final JWTVerifier VERIFIER = JWT.require(ALGORITHM).build();

    /**
     * Generates a token valid for 24 hours.
     *
     * @param userId user identifier
     * @param username username
     * @return signed JWT
     */
    public static String generateToken(Long userId, String username) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + EXPIRE_TIME);
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("username", username)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .sign(ALGORITHM);
    }

    /**
     * Parses a token and returns its user identifier.
     *
     * @param token signed JWT
     * @return user identifier
     * @throws JWTVerificationException if the token is invalid or expired
     */
    public static Long parseToken(String token) {
        DecodedJWT decodedJWT = VERIFIER.verify(token);
        return Long.valueOf(decodedJWT.getSubject());
    }

    /**
     * Checks whether a token is valid.
     *
     * @param token signed JWT
     * @return true when valid, false otherwise
     */
    public static boolean validateToken(String token) {
        try {
            VERIFIER.verify(token);
            return true;
        } catch (JWTVerificationException | IllegalArgumentException exception) {
            return false;
        }
    }
}
