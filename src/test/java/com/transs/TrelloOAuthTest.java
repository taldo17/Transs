package com.transs;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

public class TrelloOAuthTest
{
    @Test
    //Taldo: find a real way to useTheOAuthGetRequestToken this in the future - combine the login with functional tests
    public void useTheOAuthGetRequestToken() throws NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeyException, URISyntaxException, ExecutionException, InterruptedException {
        TrelloOAuth trelloOauth = new TrelloOAuth();
        OAuthCredentials temporaryCredentials = trelloOauth.getTemporaryCredentials();

    }

    @Test
    public void useTheOAuthGetAccessToken() throws URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, IOException, ExecutionException, InterruptedException {
        TrelloOAuth trelloOauth = new TrelloOAuth();
        trelloOauth.getAccessCredentials("f837c33afba92a623eecb309d5e72ffd", "7a6a7d49aa5d8b93d78c3209f066f456", "f81e37b49b62bf878a26aa7caafcc503");
    }
}