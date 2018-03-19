package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.transs.AuthenticationService;
import com.transs.FinalLoginDetails;
import com.transs.TrelloOAuth;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class OAuthGetAccessTokenHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>
{
    private static final Logger LOG = Logger.getLogger(OAuthGetAccessTokenHandler.class);
    private AuthenticationService authenticationService = new TrelloOAuth();

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context)
    {
        for (String s : input.keySet())
        {
            LOG.info("Taldo: key and values are: " + s + " " + input.get(s));
        }

        Object bodyObject = input.get("body");
        if(bodyObject == null){
            LOG.error("The body is null!!!");
            return null;
        }

        JSONObject jsonObject = new JSONObject(bodyObject.toString());
        Object verifier = jsonObject.get("verifier");
        LOG.info("The verifier is " + verifier);
        Object token = jsonObject.get("token");
        LOG.info("The token is " + token);
        Object secret = jsonObject.get("secret");
        LOG.info("The secret is " + secret);
        try
        {
            FinalLoginDetails finalLoginDetails = authenticationService.getAccessCredentials(verifier.toString(), token.toString(), secret.toString());
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(finalLoginDetails)
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                    .build();
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
