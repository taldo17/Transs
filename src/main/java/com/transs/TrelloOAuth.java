package com.transs;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class TrelloOAuth implements AuthenticationService
{
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String ENC = "UTF-8";
    private static Base64 base64 = new Base64();
    public static final String GET_REQUEST_TOKEN_URI = "https://trello.com/1/OAuthGetRequestToken";
    public static final String AUTHORIZE_TOKEN_URI = "https://trello.com/1/OAuthAuthorizeToken?oauth_token=";
    public static final String GET_ACCESS_TOKEN_URI = "https://trello.com/1/OAuthGetAccessToken";

    @Override
    public OAuthCredentials initiate() throws
            IOException, URISyntaxException, InvalidKeyException,
            NoSuchAlgorithmException {

        HttpClient httpclient = new DefaultHttpClient();
        List<NameValuePair> qparams = createInitiateParameterList();
        addSignatureToParams(qparams, GET_REQUEST_TOKEN_URI);
        URI uri = createUri(qparams, "https", "www.trello.com", "/1/OAuthGetRequestToken");
        System.out.println("Get Token and Token Secret from:"
                + uri.toString());
        HttpResponse httpResponse = executeHttpGet(httpclient, uri);
        OAuthCredentials oAuthCredentials = extractOAuthInitial(httpResponse);
        System.out.println("oauth_token=" + oAuthCredentials.oAuthToken);
        System.out.println("oauth_secret=" + oAuthCredentials.oAuthSecret);
        return oAuthCredentials;
    }

    @Override
    public OAuthCredentials getAccessCredentials(String verifier, String token) throws IOException, InvalidKeyException, NoSuchAlgorithmException, URISyntaxException
    {
        HttpClient httpclient = new DefaultHttpClient();
        List<NameValuePair> qparams = createFinalTokenParameterList(verifier, token);
        addSignatureToParams(qparams, GET_ACCESS_TOKEN_URI);
        URI uri = createUri(qparams, "https", "www.trello.com", "/1/OAuthGetAccessToken");
        System.out.println("Getting acces token from:"
                + uri.toString());
        HttpResponse httpResponse = executeHttpGet(httpclient, uri);
        OAuthCredentials oAuthCredentials = extractOAuthInitial(httpResponse);
        System.out.println("oauth_token=" + oAuthCredentials.oAuthToken);
        System.out.println("oauth_secret=" + oAuthCredentials.oAuthSecret);
        return oAuthCredentials;
    }

    private void addSignatureToParams(List<NameValuePair> qparams, String getRequestTokenUri) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException
    {
        String signature = generateSignature(URLEncoder.encode(
                getRequestTokenUri, ENC),
                URLEncoder.encode(URLEncodedUtils.format(qparams, ENC), ENC));

        qparams.add(new BasicNameValuePair("oauth_signature", signature));
    }

    private OAuthCredentials extractOAuthInitial(HttpResponse httpResponse) throws IOException
    {
        HttpEntity entity = httpResponse.getEntity();
        StringBuilder stringBuilder = new StringBuilder();
        if (entity != null) {
            InputStream inputStream = entity.getContent();
            int len;
            byte[] tmp = new byte[2048];
            while ((len = inputStream.read(tmp)) != -1) {
                stringBuilder.append(new String(tmp, 0, len, ENC));
            }
        }
        String reportString = stringBuilder.toString();
        return createOAuthInitial(reportString);
    }

    private OAuthCredentials createOAuthInitial(String responseString)
    {
        //Example response: oauth_token=hh5s93j4hdidpola&oauth_token_secret=hdhd0244k9j7ao03&oauth_callback_confirmed=true
        String[] splitByEquals = responseString.split("=");
        String oAuthToken = splitByEquals[1].split("&")[0];
        String oAuthSecret = splitByEquals[2].split("&")[0];
        return new OAuthCredentials(oAuthToken, oAuthSecret, TrelloOAuth.AUTHORIZE_TOKEN_URI);
    }

    private static String generateSignature(String url, String params)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        String stringForBaseGeneration = createStringForSignatureGeneration(url, params);

        // yea, don't ask me why, it is needed to append a "&" to the end of
        // SECRET KEY.
        byte[] keyBytes = (Trello.SECRET + "&").getBytes(ENC);
        SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1);
        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(key);
        // encode it, base64 it, change it to string and return.
        return new String(base64.encode(mac.doFinal(stringForBaseGeneration.getBytes(
                ENC))), ENC).trim();
    }

    private static String createStringForSignatureGeneration(String url, String params)
    {
        /**
         * base has three parts, they are connected by "&": 1) protocol 2) URL
         * (need to be URLEncoded) 3) Parameter List (need to be URLEncoded).
         */
        StringBuilder base = new StringBuilder();
        base.append("GET&");
        base.append(url);
        base.append("&");
        base.append(params);
        System.out.println("String for oauth_signature generation:" + base);
        return base.toString();
    }

    private HttpResponse executeHttpGet(HttpClient httpclient, URI uri) throws IOException
    {
        HttpGet httpget = new HttpGet(uri);
        HttpResponse response = httpclient.execute(httpget);
        return response;
    }


    private URI createUri(List<NameValuePair> qparams, String schema, String host, String path) throws URISyntaxException
    {
        return URIUtils.createURI(schema, host, -1,
                path,
                URLEncodedUtils.format(qparams, ENC), null);
    }

    private List<NameValuePair> createInitiateParameterList()
    {
        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("oauth_callback", "https://www.transs.com/oauthCallBack"));
        addMutualParametersForAllRequests(qparams);
        return qparams;
    }

    private List<NameValuePair> createFinalTokenParameterList(String verifier, String token)
    {
        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("oauth_token", token));
        qparams.add(new BasicNameValuePair("oauth_verifier", verifier));
        addMutualParametersForAllRequests(qparams);
        return qparams;
    }

    private void addMutualParametersForAllRequests(List<NameValuePair> qparams)
    {
        qparams.add(new BasicNameValuePair("oauth_consumer_key", Trello.KEY)); // Mutual
        qparams.add(new BasicNameValuePair("oauth_nonce", ""
                + (int) (Math.random() * 100000000))); // Mutual
        qparams.add(new BasicNameValuePair("oauth_signature_method",
                "HMAC-SHA1"));// Mutual
        qparams.add(new BasicNameValuePair("oauth_timestamp", ""
                + (System.currentTimeMillis() / 1000)));//Mutual
        qparams.add(new BasicNameValuePair("oauth_version", "1.0"));//Mutual
    }

}