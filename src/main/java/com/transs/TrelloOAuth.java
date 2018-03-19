package com.transs;

import com.github.scribejava.apis.TrelloApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TrelloOAuth implements AuthenticationService {

    final OAuth10aService service = new ServiceBuilder(Trello.KEY)
            .apiKey(Trello.KEY)
            .apiSecret(Trello.SECRET)
            .callback("transs://www.transs.com/#/oauthcallback/")
            .build(TrelloApi.instance());

    public static final String AUTHORIZE_TOKEN_URI = "https://trello.com/1/OAuthAuthorizeToken?oauth_token=";


    @Override
    public OAuthCredentials getTemporaryCredentials() throws IOException, ExecutionException, InterruptedException {
        final OAuth1RequestToken requestToken = service.getRequestToken();
        OAuthCredentials oAuthCredentials = new OAuthCredentials(requestToken.getToken(), requestToken.getTokenSecret(), AUTHORIZE_TOKEN_URI);
        return oAuthCredentials;
    }

    @Override
    public FinalLoginDetails getAccessCredentials(String verifier, String token, String secret) throws IOException, ExecutionException, InterruptedException {
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, secret);
        final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, verifier);
        List<BoardDetails> boardDetailsList = getBoardDetailsList(accessToken);
        FinalLoginDetails finalLoginDetails = new FinalLoginDetails(accessToken.getToken(), boardDetailsList);
        return finalLoginDetails;
    }

    private List<BoardDetails> getBoardDetailsList(OAuth1AccessToken accessToken) throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.trello.com/1/members/me/boards");
        service.signRequest(accessToken, request);
        final Response response = service.execute(request);
        String body = response.getBody();
        List<BoardDetails> boardDetailsList = new ArrayList<>();
        JSONArray jsonarray = new JSONArray(body);
        for (int i = 0; i < jsonarray.length(); i++) {
            extractBoardDetails(boardDetailsList, jsonarray, i);
        }
        return boardDetailsList;
    }

    private void extractBoardDetails(List<BoardDetails> boardDetailsList, JSONArray jsonarray, int i) {
        JSONObject jsonobject = jsonarray.getJSONObject(i);
        String name = jsonobject.getString("name");
        String id = jsonobject.getString("id");
        boardDetailsList.add(new BoardDetails(id, name));
    }
}
