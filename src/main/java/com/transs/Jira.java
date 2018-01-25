package com.transs;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;


public class Jira implements ALMProvider
{
    private static String auth = new String(Base64.encode("Eli.Zakashansky:Z2k2sh2nsky"));
    private final Client client = Client.create();

    @Override
    public void updateWorkItems(Set<WorkItemDetails> workItemDetails)
    {
        for (WorkItemDetails workItemDetail : workItemDetails)
        {
            updateWorkItem(Integer.parseInt(workItemDetail.id), workItemDetail.newState);
        }
    }

    @Override
    public String getWorkItemStatus(String id)
    {
        String url = "https://transs.atlassian.net/rest/api/2/issue/TRANSPARK-" + id;
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").get(ClientResponse.class);
        String jsonResponse = response.getEntity(String.class);
        JSONObject answer = new JSONObject(jsonResponse);
        JSONObject status = answer.getJSONObject("fields").getJSONObject("status");
        Object name = status.get("name");
        System.out.println("Status is : " + name);
        return (String)name;
    }


    public void updateWorkItem(int Id, String statusName)
    {
        try
        {
            String transitionId = getTransitionId(Id, statusName);
            if(transitionId == null){
                System.out.println("No need for an update on work item: " + Id);
                return;
            }
            updateState(Id, transitionId);
            addComment(Id);
            System.out.println("Finished");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Taldo - return here and activate the search ability
    private void search(String term){
        String url = "https://transs.atlassian.net/rest/api/2/search";
        System.out.println(url);

        String query = "Summary ~ \"" + term + "\"";

        Collection<String> fieldsCollection = new ArrayList<String>();
        fieldsCollection.add("id");
        fieldsCollection.add("summary");
        JSONArray fieldJSON = new JSONArray(fieldsCollection);
        String fieldsString = fieldJSON.toString();


        JSONObject data = new JSONObject();
        data.put("jql", query);
        data.put("startAt", 0);
        data.put("maxResults", 15);
        data.put("fieldsByKeys", false);
        data.put("fields", fieldJSON);

        System.out.println("Data" + data.toString());

        WebResource webResource = client.resource(url);
        ClientResponse response  = webResource.header("Authorization", "Basic " + auth)
                .type("application/json")
                .post(ClientResponse.class, data.toString());
        System.out.println("Status = " + response.getStatus());

        String jsonResponse = response.getEntity(String.class);
        System.out.println(jsonResponse);
        JSONObject answer = new JSONObject(jsonResponse);

    }


    private String getTransitionId(int Id, String statusName)
    {
        String transitionId = null;
        String url = "https://transs.atlassian.net/rest/api/2/issue/TRANSPARK-" + Id + "/transitions";
        System.out.println(url);
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").get(ClientResponse.class);
        String jsonResponse = response.getEntity(String.class);
        JSONObject answer = new JSONObject(jsonResponse);
        JSONArray transitions = answer.getJSONArray("transitions");
        for (int i = 0; i < transitions.length(); i++)
        {
            JSONObject transition = transitions.getJSONObject(i);
            if (transition.getString("name").equals(statusName))
            {
                transitionId =  transition.getString("id");
            }
        }
        return transitionId;
    }

    private void updateState(int Id, String transitionId)
    {
        String url = "https://transs.atlassian.net/rest/api/2/issue/TRANSPARK-" + Id + "/transitions";
        System.out.println(url);

        JSONObject transitionJson = new JSONObject();
        transitionJson.accumulate("id", transitionId);

        JSONObject data = new JSONObject();
        data.accumulate("transition", transitionJson);

        WebResource webResource = client.resource(url);

        ClientResponse response = webResource.header("Authorization", "Basic " + auth)
                .type("application/json")
                .post(ClientResponse.class, data.toString());
        System.out.println("Status = " + response.getStatus());
    }

    private void addComment(int Id)
    {
        JSONObject data = new JSONObject();
        data.accumulate("body", "Updated by TRANSS - Build Trust Throughout Transparency!");
        String url = "https://transs.atlassian.net/rest/api/2/issue/TRANSPARK-" + Id + "/comment";
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth)
                .type("application/json")
                .post(ClientResponse.class, data.toString());
        System.out.println("Status = " + response.getStatus());
    }
}
