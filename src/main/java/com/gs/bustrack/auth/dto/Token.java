package com.gs.bustrack.auth.dto;

/**
 * 
 * @author Carlos Juarez
 */
public class Token {
    
    private String accessToken;
    
    private String tokenType = "Bearer";

    public Token() {
    }
    
    public Token(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
