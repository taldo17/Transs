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

    //since transs not yet impersonates a specific client, this class works with fixed credentials.
    //to make this class funciotnal replace it with actual credentials
    private static String auth = new String(Base64.encode("<username>:<password>"));
    private final Client client = Client.create();

    //The url prefix is currently hard coded to transs specific jira project - the url will change with a different
    //login to the different projects
    private static final String URL_PREFIX = "https://transs.atlassian.net/rest/api/2/";

    @Override
    public void updateWorkItems(Set<WorkItemDetails> workItemDetails, String boardId, String token)
    {
        for (WorkItemDetails workItemDetail : workItemDetails)
        {
            updateWorkItem(Integer.parseInt(workItemDetail.id), workItemDetail.newState);
        }
    }

    @Override
    public String getWorkItemStatus(String id, String boardId, String token)
    {
        String url = URL_PREFIX +"issue/TRANSPARK-" + id;
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


    private void updateWorkItem(int id, String statusName)
    {
        try
        {
            String transitionId = getTransitionId(id, statusName);
            if(transitionId == null){
                System.out.println("No need for an update on work item: " + id);
                return;
            }
            updateState(id, transitionId);
            addComment(id);
            System.out.println("Finished");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Taldo - search is currently not active and not used
    private void search(String term){
        String url = URL_PREFIX + "search";
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


    private String getTransitionId(int id, String statusName)
    {
        String transitionId = null;
        String url = createTransitionsUrl(id);
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

    private void updateState(int id, String transitionId)
    {
        String url = createTransitionsUrl(id);
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
        data.accumulate("body", TranssService.TRANSS_UPDATE_COMMENT);
        String url = URL_PREFIX + "issue/TRANSPARK-" + Id + "/comment";
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth)
                .type("application/json")
                .post(ClientResponse.class, data.toString());
        System.out.println("Status = " + response.getStatus());
    }

    private String createTransitionsUrl(int id){
        return URL_PREFIX + "issue/TRANSPARK-" + id + "/transitions";
    }

}
