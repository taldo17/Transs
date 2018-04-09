package com.transs;

import org.apache.http.HttpHeaders;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

public class VisualStudioOnline implements ALMProvider {

    private HashMap<String, JSONArray> operationArray;

    VisualStudioOnline(){
        operationArray = new HashMap<>(3);
        operationArray.put("todo", generateOperationArrayItem("New", "Moved to the backlog"));
        operationArray.put("inprogress", generateOperationArrayItem("Active", "Implementation started"));
        operationArray.put("done", generateOperationArrayItem("Closed","Acceptance tests pass"));
    }

    private JSONArray generateOperationArrayItem(String state, String reason) {
        JSONArray data = new JSONArray();
        JSONObject operation = new JSONObject();
        operation.put("op", "replace");
        operation.put("path", "/fields/System.State");
        operation.put("value", state );
        data.put(operation);
        operation = new JSONObject();
        operation.put("op", "replace");
        operation.put("path", "/fields/System.Reason");
        operation.put("value", reason);
        data.put(operation);
        return data;
    }


    @Override
    public void updateWorkItems(Set<WorkItemDetails> workItemDetails, String boardId, String token) {
        for (WorkItemDetails wid: workItemDetails) {
            if (!updateWorkItem(wid)) {
                String currentState = getWorkItemStatus(wid.id, null, null);
                if (isStatesNotSequential(currentState, wid.newState))//ideally this should not be in production code
                {
                    WorkItemDetails intermediate = new WorkItemDetails(0, 0, wid.id, "todo");
                    updateWorkItem(intermediate);
                    updateWorkItem(wid);
                }
            }
        }
    }

    private boolean isStatesNotSequential(String initialState, String finalState){
        return (initialState.equalsIgnoreCase("done") && finalState.equalsIgnoreCase("inprogress"));
    }
    private boolean updateWorkItem(WorkItemDetails details) {
        String vsoID = "" + getCard(details.id).getInt("id");

        HttpPatch patchRequest = new HttpPatch(
                "https://transs.visualstudio.com/DefaultCollection/_apis/wit/workitems/"
                        + vsoID
                        + "?api-version=1.0");
        String operationKey = details.newState.replace(" ", "").toLowerCase();
        JSONArray data = operationArray.get(operationKey);
        patchRequest.setEntity(generateStringEntity(data));
        patchRequest.setHeader("Authorization", "Basic " + getVsoAuthenticationString());

        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(patchRequest)) {
                System.out.println("Response is " + response.getStatusLine());
                return 200 == response.getStatusLine().getStatusCode();
            }
        }catch(IOException ioe){
            System.out.println("Error patching request : "+ ioe.toString());
        }
        return false;
    }

    private StringEntity generateStringEntity(JSONArray data) {
        StringEntity entity = null;
        try {

            entity =  new StringEntity(data.toString());
            entity.setContentType("application/json-patch+json");
        } catch (UnsupportedEncodingException e) {
            System.out.println("unable to create string entity " + e.toString());
        }
        return  entity;
    }

    @Override
    public String getWorkItemStatus(String id, String boardId, String token) {
        JSONObject json = getCard(id);
        System.out.println(json);
        JSONObject fields = json.getJSONObject("fields");
        return fields.getString("System.BoardColumn");
    }

    private JSONObject getCard(String identifier) {
        String url = "https://transs.visualstudio.com/DefaultCollection/_apis/wit/wiql?api-version=1.0";
        //"https://transs.visualstudio.com/MyFirstProject/_backlogs/board?quickstart=Kanban
        String wiqlQuery = "Select [ID], [Rev] From WorkItems Where [Title] Contains Words '" + identifier +"'";

        JSONObject data = new JSONObject();
        data.put("query", wiqlQuery);


        RequestBuilder builder = RequestBuilder.post()
                .setUri(url)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setEntity(EntityBuilder.create().setText(data.toString()).build());

        JSONObject jresponse = ExecuteRequest(builder);
        String wiURL = jresponse.getJSONArray("workItems").getJSONObject(0).getString("url");
        return getSpecificWIObject(wiURL);
    }

    private JSONObject getSpecificWIObject(String wiURL) {
        RequestBuilder builder = RequestBuilder.get()
                .setUri(wiURL)
                .setHeader("Authorization", "Basic " + getVsoAuthenticationString())
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        return ExecuteRequest(builder);

    }
    private JSONObject ExecuteRequest(RequestBuilder builder) {
        builder.setHeader("Authorization", "Basic " + getVsoAuthenticationString());//otherwise anauthorized
        HttpUriRequest request = builder.build();
        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                System.out.println("Status = " + response.getStatusLine());
                ResponseHandler<String> handler = new BasicResponseHandler();
                String content = handler.handleResponse(response);
                return new JSONObject(content);
            }
        }
        catch (IOException ioe) {
            System.out.println("Error executing request " + ioe.toString());
        }
        return new JSONObject();
    }

    private String getVsoAuthenticationString() {
        String pat = "";//put pat here
        String authString = ":" + pat;
        byte[] asciiBytes = authString.getBytes(StandardCharsets.US_ASCII);
        return java.util.Base64.getEncoder().encodeToString(asciiBytes);
    }
}
