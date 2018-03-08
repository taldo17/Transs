package com.transs;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public interface AuthenticationService
{
    OAuthCredentials initiate() throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException, ClientProtocolException, IOException, URISyntaxException, InvalidKeyException;

    OAuthCredentials getAccessCredentials(String verifier, String token) throws IOException, InvalidKeyException, NoSuchAlgorithmException, URISyntaxException;
}
