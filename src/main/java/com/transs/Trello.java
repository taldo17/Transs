package com.transs;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

public class Trello implements ALMProvider {

    private final Client client = Client.create();
    private HashMap<String, String> statusIdToNameMapping;
    private HashMap<String, String> statusNameToIdMapping;

    private static final String INITIAL_URL_PREFIX = "https://api.trello.com/1/";
    public static final String KEY = "";
    private String userAccessToken;
    private String userBoardId;

    public Trello(){
    }

    private void createInitialExpectedLists() {
        JSONArray lists = getLists();
        if(statusIdToNameMapping!= null && statusNameToIdMapping != null){
            return;
        }
        statusIdToNameMapping = new HashMap<>(4);
        statusNameToIdMapping = new HashMap<>(4);
        statusIdToNameMapping.put("none", "Not found");

        for (int i = 0; i < lists.length(); i++) {
            fillTheMapping(lists, i);
        }
    }

    private void fillTheMapping(JSONArray lists, int i) {
        JSONObject list = lists.getJSONObject(i);
        String id = list.getString("id");
        String name = list.getString("name");
        System.out.println("adding id = " + id + ", name = " + name);
        statusIdToNameMapping.put(id, name);
        statusNameToIdMapping.put(name, id);
    }

    @Override
    public void updateWorkItems(Set<WorkItemDetails> workItemDetails, String boardId, String token)
    {
        userAccessToken = token;
        userBoardId = boardId;
        createInitialExpectedLists();
        for (WorkItemDetails workItemDetail : workItemDetails)
        {
            updateWorkItem(workItemDetail);

        }
    }



    @Override
    public String getWorkItemStatus(String id, String boardId, String token) {
        userAccessToken = token;
        userBoardId = boardId;
        createInitialExpectedLists();
        return statusIdToNameMapping.get(getCard(id).get("idList"));
    }

    private void updateWorkItem(WorkItemDetails workItemDetail)
    {
        JSONObject card = getCard(workItemDetail.id);
        String url = INITIAL_URL_PREFIX + "cards/"+ card.getString("id") +"?desc="+ getDescription(card) +"&idList=" + statusNameToIdMapping.get(workItemDetail.newState) + "&" + trelloAuthenticationPostfix();
        System.out.println(url);
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.put(ClientResponse.class);
        System.out.println("Status = " + response.getStatus());
        String jsonResponse = response.getEntity(String.class);
        System.out.println(jsonResponse);
    }

    private static String getDescription(JSONObject card) {
        String description = card.getString("desc");
        if (!descriptionIsAlreadyUpdatedByTranss(description))
        {
            description += "%0AUpdated%20by%20TRANSS%20-%20Build%20Trust%20Throughout%20Transparency!";
        }
        description = verifyUrlIsNotCorrupted(description);
        return description;
    }

    private static boolean descriptionIsAlreadyUpdatedByTranss(String description)
    {
        return description.endsWith(TranssService.TRANSS_UPDATE_COMMENT);
    }

    private static String verifyUrlIsNotCorrupted(String description)
    {
        description = description.replace(" ", "%20");
        description = description.replace("\n", "%0A");
        return description;
    }



    private JSONArray getLists(){
        String url = INITIAL_URL_PREFIX + "boards/" + userBoardId + "/lists?" + trelloAuthenticationPostfix();
        return httpGetOnUrl(url);
    }

    private JSONArray getCards(){
        String url = INITIAL_URL_PREFIX + "boards/" + userBoardId + "/cards?" + trelloAuthenticationPostfix();
        return httpGetOnUrl(url);
    }

    private JSONArray httpGetOnUrl(String url) {
        System.out.println(url);
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.get(ClientResponse.class);
        System.out.println("Status = " + response.getStatus());

        String jsonResponse = response.getEntity(String.class);
        System.out.println(jsonResponse);
        return new JSONArray(jsonResponse);
    }



    private JSONObject getCard(String id){
        JSONArray cards = getCards();
        for (int i = 0; i < cards.length(); i++) {
            JSONObject card = cards.getJSONObject(i);
            String cardName = card.getString("name");
            if( cardName.equals(id)){
                return card;
            }
        }
        return emptyCard();
    }

    private static JSONObject emptyCard()
    {
        return new JSONObject("{\"idList\":\"none\"}");
    }

    private  String trelloAuthenticationPostfix(){
        return "key=" + KEY +"&token=" + userAccessToken;
    }


}
