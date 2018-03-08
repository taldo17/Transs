package com.transs;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class TrelloOauthTest
{
    @Test
    //Taldo: find a real way to useTheOauthGetRequestToken this in the future - combine the login with functional tests
    public void useTheOauthGetRequestToken() throws NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeyException, URISyntaxException
    {
        TrelloOAuth trelloOauth = new TrelloOAuth();
        trelloOauth.initiate();
    }

    @Test
    public void useTheOauthGetAccessToken() throws URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, IOException
    {
        TrelloOAuth trelloOauth = new TrelloOAuth();
        trelloOauth.getAccessCredentials("123", "28aa6b90ceb22ca9d0ff9d8f1e8bef90");
    }
}