package com.transs;

import java.util.Set;

public class TranssService
{
    private ImageRecognition imageRecognition;
    private ALMProvider almProvider;

    public TranssService(ImageRecognition imageRecognition, ALMProvider almProvider)
    {
        this.imageRecognition = imageRecognition;
        this.almProvider = almProvider;
    }

    public void analyzeImageAndUpdateALM(byte[] rawData)
    {
        Set<WorkItemDetails> workItemDetails = imageRecognition.analyzeImage(rawData);
        almProvider.updateWorkItems(workItemDetails);
    }

}
