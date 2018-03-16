package com.transs;

import com.github.scribejava.apis.TrelloApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TrelloOAuth implements AuthenticationService {

    final OAuth10aService service = new ServiceBuilder(Trello.KEY)
            .apiKey(Trello.KEY)
            .apiSecret(Trello.SECRET)
            .callback("Taldo")//"transs://www.transs.com/#/oauthcallback/")
            .build(TrelloApi.instance());

    public static final String AUTHORIZE_TOKEN_URI = "https://trello.com/1/OAuthAuthorizeToken?oauth_token=";


    @Override
    public OAuthCredentials getTemporaryCredentials() throws IOException, ExecutionException, InterruptedException {
        final OAuth1RequestToken requestToken = service.getRequestToken();
        OAuthCredentials oAuthCredentials = new OAuthCredentials(requestToken.getToken(), requestToken.getTokenSecret(), AUTHORIZE_TOKEN_URI);
        return oAuthCredentials;
    }

    @Override
    public TokenAndBoards getAccessCredentials(String verifier, String token, String secret) throws IOException, ExecutionException, InterruptedException {
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, secret);
        final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, verifier);
        OAuthCredentials oAuthCredentials = new OAuthCredentials(accessToken.getToken(), accessToken.getTokenSecret(), "");
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.trello.com/1/members/me/boards");
        service.signRequest(accessToken, request); // the access token from step 4
        final Response response = service.execute(request);
        String body = response.getBody();
        JSONObject jsonObject = new JSONObject(body);
        Object name = jsonObject.get("name");
        Object id = jsonObject.get("id");
        HashMap<String, String> nnn = new HashMap<>();
        nnn.put(id.toString(), name.toString());
        TokenAndBoards tokenAndBoards = new TokenAndBoards(nnn, oAuthCredentials.oAuthToken);
        return tokenAndBoards;
    }
}
