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
}