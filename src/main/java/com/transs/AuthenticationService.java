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
    OAuthInitial initiate() throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException, ClientProtocolException, IOException, URISyntaxException, InvalidKeyException;
}
