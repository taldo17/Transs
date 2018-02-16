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
    private HashMap<String, String> statusNameToUdMapping;

    public Trello(){
        JSONArray lists = getLists(client);
        statusIdToNameMapping = new HashMap<String, String>(4);
        statusNameToUdMapping = new HashMap<String, String>(4);
        statusIdToNameMapping.put("none", "Not found");

        for (int i = 0; i < lists.length(); i++) {
            JSONObject list = lists.getJSONObject(i);
            String id = list.getString("id");
            String name = list.getString("name");
            System.out.println("adding id = " + id + ", name = " + name);
            statusIdToNameMapping.put(id, name);
            statusNameToUdMapping.put(name, id);
        }
    }



    @Override
    public void updateWorkItems(Set<WorkItemDetails> workItemDetails)
    {
        for (WorkItemDetails workItemDetail : workItemDetails)
        {
            updateWorkItem(workItemDetail.id, workItemDetail.newState);
        }
    }

    private void updateWorkItem(String id, String status){
        JSONObject card = getCard(id, client);

        updateWorkItemInTrello(statusNameToUdMapping.get(status),
                card.getString("id"),
                getDescription(card),
                client);
    }

    private static String getDescription(JSONObject card) {
        String description = card.getString("desc");
        if(!description.endsWith("Updated by TRANSS - Build Trust Throughout Transparency!"))
        {
            description += "%0AUpdated%20by%20TRANSS%20-%20Build%20Trust%20Throughout%20Transparency!";
        }

        //make sure it will not corrupt the url
        description = description.replace(" ", "%20");
        description = description.replace("\n", "%0A");

        return description;
    }


    private static void updateWorkItemInTrello(String newListId, String cardId, String description, Client client){
        //String cardID = "5a85e78bbb28cd0af31da3a5";
        String url = "https://api.trello.com/1/cards/"+ cardId +"?desc="+description+"&idList=" + newListId + "&" + trelloAuthenticationPostfix();

        System.out.println(url);
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.put(ClientResponse.class);
        System.out.println("Status = " + response.getStatus());

        String jsonResponse = response.getEntity(String.class);
        System.out.println(jsonResponse);

    }

    @Override
    public String getWorkItemStatus(String id) {
        return statusIdToNameMapping.get(getCard(id, client).get("idList"));
    }


    private static JSONArray getLists(Client client){
        String url = "https://api.trello.com/1/boards/5a85e015c8b0ad48292bdf26/lists?" + trelloAuthenticationPostfix();
        return getJsonArrayFromURL(client, url);
    }

    private static JSONArray getCards(Client client){
        String url = "https://api.trello.com/1/boards/5a85e015c8b0ad48292bdf26/cards?" + trelloAuthenticationPostfix();
        return getJsonArrayFromURL(client, url);
    }

    private static JSONArray getJsonArrayFromURL(Client client, String url) {
        System.out.println(url);

        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.get(ClientResponse.class);
        System.out.println("Status = " + response.getStatus());

        String jsonResponse = response.getEntity(String.class);
        System.out.println(jsonResponse);
        return new JSONArray(jsonResponse);
    }



    private static JSONObject getCard(String id, Client client){
        JSONArray cards = getCards(client);
        for (int i = 0; i < cards.length(); i++) {
            JSONObject card = cards.getJSONObject(i);
            String cardName = card.getString("name");
            if( cardName.equals(id)){
                return card;
            }
        }

        //return empty card
        return new JSONObject("{\"idList\":\"none\"}");
    }

    private static String trelloAuthenticationPostfix(){
        String trelloApiKey = ""; //originated by trello
        String trelloToken = ""; //originted by trello
        return "key=" + trelloApiKey +"&token=" + trelloToken;
    }


}
