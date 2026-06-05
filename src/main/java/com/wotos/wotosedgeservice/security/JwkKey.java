package com.wotos.wotosedgeservice.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single key entry from a JWKS (JSON Web Key Set) response.
 * Only RSA keys ({@code kty=RSA}) are used for RS256 validation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwkKey {

    @JsonProperty("kid")
    private String kid;

    @JsonProperty("kty")
    private String kty;

    @JsonProperty("use")
    private String use;

    @JsonProperty("alg")
    private String alg;

    /** Base64url-encoded RSA modulus. */
    @JsonProperty("n")
    private String n;

    /** Base64url-encoded RSA public exponent. */
    @JsonProperty("e")
    private String e;

    public JwkKey() {}

    public String getKid() { return kid; }
    public void setKid(String kid) { this.kid = kid; }

    public String getKty() { return kty; }
    public void setKty(String kty) { this.kty = kty; }

    public String getUse() { return use; }
    public void setUse(String use) { this.use = use; }

    public String getAlg() { return alg; }
    public void setAlg(String alg) { this.alg = alg; }

    public String getN() { return n; }
    public void setN(String n) { this.n = n; }

    public String getE() { return e; }
    public void setE(String e) { this.e = e; }
}
