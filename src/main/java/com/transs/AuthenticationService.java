package com.transs;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface AuthenticationService
{
    OAuthCredentials getTemporaryCredentials() throws IOException, ExecutionException, InterruptedException;

    FinalLoginDetails getAccessCredentials(String verifier, String token, String secret) throws IOException, ExecutionException, InterruptedException;
}
