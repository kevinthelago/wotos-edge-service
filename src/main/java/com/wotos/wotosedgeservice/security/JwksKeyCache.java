package com.wotos.wotosedgeservice.security;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of RSA public keys derived from the user-service JWKS.
 *
 * <p>Keys are loaded lazily on first use. When a JWT references an unknown
 * {@code kid} the cache is refreshed once from the JWKS endpoint — this
 * handles key rotation without a periodic scheduler. A {@code synchronized}
 * block prevents thundering-herd re-fetches under concurrent load.
 */
@Component
public class JwksKeyCache {

    private final JwksClient jwksClient;
    private final Map<String, PublicKey> cache = new ConcurrentHashMap<>();
    private volatile boolean loaded = false;

    public JwksKeyCache(JwksClient jwksClient) {
        this.jwksClient = jwksClient;
    }

    /**
     * Returns the RSA public key for {@code kid}, or {@code null} if the key
     * is not present even after a cache refresh.
     */
    public PublicKey getKey(String kid) {
        if (!loaded) {
            loadKeys();
        }
        PublicKey key = cache.get(kid);
        if (key != null) {
            return key;
        }
        // Unknown kid — refresh once to handle key rotation
        loadKeys();
        return cache.get(kid);
    }

    private synchronized void loadKeys() {
        // Double-checked: another thread may have already refreshed
        JwkSet jwkSet = jwksClient.getJwks();
        if (jwkSet == null || jwkSet.getKeys() == null) {
            return;
        }
        Map<String, PublicKey> fresh = new ConcurrentHashMap<>();
        List<JwkKey> keys = jwkSet.getKeys();
        for (JwkKey jwkKey : keys) {
            if (!"RSA".equals(jwkKey.getKty())) continue;
            if (jwkKey.getN() == null || jwkKey.getE() == null) continue;
            try {
                PublicKey publicKey = buildRsaPublicKey(jwkKey.getN(), jwkKey.getE());
                fresh.put(jwkKey.getKid(), publicKey);
            } catch (Exception ignored) {
                // Skip malformed keys; the remaining valid ones still load
            }
        }
        cache.clear();
        cache.putAll(fresh);
        loaded = true;
    }

    private static PublicKey buildRsaPublicKey(String n, String e) throws Exception {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        BigInteger modulus = new BigInteger(1, decoder.decode(padBase64Url(n)));
        BigInteger exponent = new BigInteger(1, decoder.decode(padBase64Url(e)));
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /**
     * Base64url strings in JWKS may omit padding; this restores it so the
     * standard {@link Base64#getUrlDecoder()} can parse them reliably.
     */
    static String padBase64Url(String input) {
        int mod = input.length() % 4;
        if (mod == 0) return input;
        if (mod == 1) throw new IllegalArgumentException("Invalid base64url length: " + input.length());
        StringBuilder sb = new StringBuilder(input);
        while (sb.length() % 4 != 0) sb.append('=');
        return sb.toString();
    }
}
