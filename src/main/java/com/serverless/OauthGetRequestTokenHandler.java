package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.transs.AuthenticationService;
import com.transs.OAuthInitial;
import com.transs.Trello;
import com.transs.TrelloOAuth;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import java.util.Map;

public class OauthGetRequestTokenHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>
{
    private static final Logger LOG = Logger.getLogger(OauthGetRequestTokenHandler.class);
    private AuthenticationService authenticationService = new TrelloOAuth();

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> stringObjectMap, Context context)
    {
        try
        {
            OAuthInitial oAuthInitial = authenticationService.initiate();
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(oAuthInitial)
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                    .build();
            //Taldo: what about the redirect
//            return Response.temporaryRedirect(getRedirectURI()).status(200).build();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchProviderException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        return null;

    }

    private static URI getRedirectURI() {
        URI uri = null;
        try {
            //Taldo: change that to the token/ secret or what ever is needed
            uri = new URI(TrelloOAuth.AUTHORIZE_TOKEN_URI + Trello.KEY);
        }
        catch (URISyntaxException urise){
            LOG.error("exception occured on URI creation!!!"  + urise);
        }
        return uri;
    }

}
