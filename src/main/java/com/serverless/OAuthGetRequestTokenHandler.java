package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.transs.AuthenticationService;
import com.transs.OAuthCredentials;
import com.transs.TrelloOAuth;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class OAuthGetRequestTokenHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>
{
    private static final Logger LOG = Logger.getLogger(OAuthGetRequestTokenHandler.class);
    private AuthenticationService authenticationService = new TrelloOAuth();

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> stringObjectMap, Context context)
    {
        try
        {
            OAuthCredentials oAuthCredentials = authenticationService.getTemporaryCredentials();
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(oAuthCredentials)
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                    .build();
            //Taldo: what about the redirect
//            return Response.temporaryRedirect(getRedirectURI()).status(200).build();
        }

        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;

    }

}
