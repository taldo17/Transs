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


    public OAuthInitial initiate() throws
            IOException, URISyntaxException, InvalidKeyException,
            NoSuchAlgorithmException {

        HttpClient httpclient = new DefaultHttpClient();
        List<NameValuePair> qparams = createParameterList();
        String signature = generateSignature(URLEncoder.encode(
                GET_REQUEST_TOKEN_URI, ENC),
                URLEncoder.encode(URLEncodedUtils.format(qparams, ENC), ENC));

        qparams.add(new BasicNameValuePair("oauth_signature", signature));
        URI uri = createGetTokenUri(qparams);
        System.out.println("Get Token and Token Secret from:"
                + uri.toString());
        HttpResponse httpResponse = executeHttpGet(httpclient, uri);
        OAuthInitial oAuthInitial = extractOAuthInitial(httpResponse);
        System.out.println("oauth_token=" + oAuthInitial.oAuthToken);
        System.out.println("oauth_secret=" + oAuthInitial.oAuthSecret);
        return oAuthInitial;
    }

    private OAuthInitial extractOAuthInitial(HttpResponse httpResponse) throws IOException
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

    private OAuthInitial createOAuthInitial(String reportString)
    {
        String[] splitByEquals = reportString.split("=");
        String oAuthToken = splitByEquals[1].split("&")[0];
        String oAuthSecret = splitByEquals[2].split("&")[0];
        return new OAuthInitial(oAuthToken, oAuthSecret, TrelloOAuth.AUTHORIZE_TOKEN_URI);
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

    private URI createGetTokenUri(List<NameValuePair> qparams) throws URISyntaxException
    {
        return URIUtils.createURI("https", "www.trello.com", -1,
                    "/1/OAuthGetRequestToken",
                    URLEncodedUtils.format(qparams, ENC), null);
    }

    private List<NameValuePair> createParameterList()
    {
        List<NameValuePair> qparams = new ArrayList<>();
        //Taldo: Complete Callback here:
        qparams.add(new BasicNameValuePair("oauth_callback", "oob"));
        qparams.add(new BasicNameValuePair("oauth_consumer_key", Trello.KEY));
        qparams.add(new BasicNameValuePair("oauth_nonce", ""
                + (int) (Math.random() * 100000000)));
        qparams.add(new BasicNameValuePair("oauth_signature_method",
                "HMAC-SHA1"));
        qparams.add(new BasicNameValuePair("oauth_timestamp", ""
                + (System.currentTimeMillis() / 1000)));
        qparams.add(new BasicNameValuePair("oauth_version", "1.0"));
        return qparams;
    }

}