package com.wotos.wotosedgeservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JwtValidatorTest {

    private JwksKeyCache keyCache;
    private JwtValidator validator;

    private KeyPair keyPair;
    private static final String KID = "test-key-1";

    @Before
    public void setUp() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();

        keyCache = mock(JwksKeyCache.class);
        when(keyCache.getKey(KID)).thenReturn(keyPair.getPublic());

        validator = new JwtValidator(keyCache, new ObjectMapper());
    }

    @Test
    public void acceptsValidToken() throws Exception {
        long exp = System.currentTimeMillis() / 1000L + 3600;
        String token = buildToken(keyPair.getPrivate(), KID, exp, "user99");

        Map<String, Object> claims = validator.validate(token);

        assertEquals("user99", claims.get("sub"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsExpiredToken() throws Exception {
        long exp = System.currentTimeMillis() / 1000L - 1;
        String token = buildToken(keyPair.getPrivate(), KID, exp, "user1");

        validator.validate(token);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsWrongSignature() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        PrivateKey otherKey = gen.generateKeyPair().getPrivate();

        long exp = System.currentTimeMillis() / 1000L + 3600;
        String token = buildToken(otherKey, KID, exp, "user1");

        validator.validate(token);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedToken() {
        validator.validate("not.a.jwt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsTwoPartToken() {
        validator.validate("only.twoparts");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownKid() throws Exception {
        when(keyCache.getKey("unknown-kid")).thenReturn(null);
        long exp = System.currentTimeMillis() / 1000L + 3600;
        String token = buildToken(keyPair.getPrivate(), "unknown-kid", exp, "user1");

        validator.validate(token);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonRs256Algorithm() throws Exception {
        // Build a token with alg:HS256 in the header
        String header = b64Url("{\"alg\":\"HS256\",\"kid\":\"" + KID + "\"}");
        String payload = b64Url("{\"sub\":\"user1\",\"exp\":" + (System.currentTimeMillis() / 1000L + 3600) + "}");
        String token = header + "." + payload + ".invalidsig";

        validator.validate(token);
    }

    // --- helpers ---

    private String buildToken(PrivateKey privateKey, String kid, long exp, String sub) throws Exception {
        String header = b64Url("{\"alg\":\"RS256\",\"kid\":\"" + kid + "\"}");
        String payload = b64Url("{\"sub\":\"" + sub + "\",\"exp\":" + exp + "}");
        String signingInput = header + "." + payload;

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(sig.sign());

        return signingInput + "." + signature;
    }

    private static String b64Url(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                json.getBytes(StandardCharsets.UTF_8));
    }
}
