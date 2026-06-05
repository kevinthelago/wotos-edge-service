package com.wotos.wotosedgeservice.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

/**
 * Validates RS256 JSON Web Tokens without Spring Security.
 *
 * <p>Validation steps:
 * <ol>
 *   <li>Parse the JWT header to extract {@code alg} (must be RS256) and {@code kid}.</li>
 *   <li>Resolve the RSA public key for {@code kid} from the JWKS cache.</li>
 *   <li>Verify the signature over {@code base64url(header).base64url(payload)}.</li>
 *   <li>Parse the payload and assert the {@code exp} claim has not passed.</li>
 * </ol>
 *
 * <p>Throws {@link IllegalArgumentException} for any validation failure so the
 * caller can respond with HTTP 401 without catching checked exceptions.
 */
@Component
public class JwtValidator {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};

    private final JwksKeyCache jwksKeyCache;
    private final ObjectMapper objectMapper;

    public JwtValidator(JwksKeyCache jwksKeyCache, ObjectMapper objectMapper) {
        this.jwksKeyCache = jwksKeyCache;
        this.objectMapper = objectMapper;
    }

    /**
     * Validates {@code token} and returns the parsed claims.
     *
     * @throws IllegalArgumentException when the token is structurally invalid,
     *                                  has an unrecognised algorithm or kid,
     *                                  fails signature verification, or is expired
     */
    public Map<String, Object> validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed JWT: expected 3 dot-separated parts");
        }

        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();

            Map<String, Object> header = objectMapper.readValue(
                    decoder.decode(JwksKeyCache.padBase64Url(parts[0])), MAP_TYPE);

            String alg = (String) header.get("alg");
            if (!"RS256".equals(alg)) {
                throw new IllegalArgumentException("Unsupported JWT algorithm: " + alg);
            }

            String kid = (String) header.get("kid");
            if (kid == null || kid.isEmpty()) {
                throw new IllegalArgumentException("JWT header missing kid");
            }

            PublicKey publicKey = jwksKeyCache.getKey(kid);
            if (publicKey == null) {
                throw new IllegalArgumentException("No key found for kid: " + kid);
            }

            // Verify RS256 signature over the original ASCII signing input
            byte[] signingInput = (parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII);
            byte[] signatureBytes = decoder.decode(JwksKeyCache.padBase64Url(parts[2]));

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(signingInput);
            if (!sig.verify(signatureBytes)) {
                throw new IllegalArgumentException("JWT signature verification failed");
            }

            Map<String, Object> claims = objectMapper.readValue(
                    decoder.decode(JwksKeyCache.padBase64Url(parts[1])), MAP_TYPE);

            Number expClaim = (Number) claims.get("exp");
            if (expClaim != null && System.currentTimeMillis() / 1000L > expClaim.longValue()) {
                throw new IllegalArgumentException("JWT has expired");
            }

            return claims;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT validation error: " + e.getMessage(), e);
        }
    }
}
