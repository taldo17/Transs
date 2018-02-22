package com.transs;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TranssSteps
{
    private Map<String, String> newStatesToImages = new HashMap<>();
    private String initialState;
    private Jira jira = new Jira();
    private Trello trello = new Trello();
    private int currentId;

    public TranssSteps()
    {
        newStatesToImages.put("To Do", "ToDo.jpeg");
        newStatesToImages.put("In Progress", "24-InProgress.jpeg");
        newStatesToImages.put("Done", "25-Done.jpeg");
    }

    @Given("^a user story with id (.*) in state (.*)$")
    public void a_user_story_in_state(int id, String currentState) throws Throwable
    {
        initialState = currentState;
        currentId = id;
        trello.updateWorkItem(String.valueOf(currentId), initialState);
        RekognitionImageRecognition rekognitionImageRecognition = new RekognitionImageRecognition();
        String actualWorkItemStatus = trello.getWorkItemStatus(String.valueOf(id));
        Assert.assertEquals(initialState, actualWorkItemStatus);
    }

    @When("^I move the story to state (.*) and use the application$")
    public void i_move_the_story_to_a_new_state_and_use_the_application(String newState) throws Throwable
    {
        String stateImageName = newStatesToImages.get(newState);
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(stateImageName).getFile());
        InputStream inputStream = new FileInputStream(file);
        byte[] imageOriginalBytes = IOUtils.toByteArray(inputStream);
        String interesting = imageOriginalBytes.toString();
        System.out.println(interesting);
        TranssService transsService = new TranssService(new RekognitionImageRecognition(), new Trello());
        transsService.analyzeImageAndUpdateALM(imageOriginalBytes);
    }



    @Then("^the story should be in state (.*)$")
    public void the_story_should_be_in_state_Final_State(String finalState) throws Throwable
    {
        Assert.assertEquals(finalState, trello.getWorkItemStatus(String.valueOf(currentId)));
    }
}
