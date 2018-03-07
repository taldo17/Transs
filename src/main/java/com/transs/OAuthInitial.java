package com.transs;

public class OAuthInitial
{
    public String oAuthToken;
    public String oAuthSecret;
    public String getRequestTokenUri;

    public OAuthInitial(String oAuthToken, String oAuthSecret, String getRequestTokenUri)
    {
        this.oAuthToken = oAuthToken;
        this.oAuthSecret = oAuthSecret;
        this.getRequestTokenUri = getRequestTokenUri;
    }
}
