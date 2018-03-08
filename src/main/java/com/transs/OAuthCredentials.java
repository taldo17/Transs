package com.transs;

public class OAuthCredentials
{
    public String oAuthToken;
    public String oAuthSecret;
    public String getRequestTokenUri;

    public OAuthCredentials(String oAuthToken, String oAuthSecret, String getRequestTokenUri)
    {
        this.oAuthToken = oAuthToken;
        this.oAuthSecret = oAuthSecret;
        this.getRequestTokenUri = getRequestTokenUri;
    }
}
