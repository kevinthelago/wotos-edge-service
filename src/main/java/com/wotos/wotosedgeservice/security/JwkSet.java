package com.wotos.wotosedgeservice.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Top-level JWKS response ({@code {"keys":[…]}}). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwkSet {

    @JsonProperty("keys")
    private List<JwkKey> keys;

    public JwkSet() {}

    public List<JwkKey> getKeys() { return keys; }
    public void setKeys(List<JwkKey> keys) { this.keys = keys; }
}
