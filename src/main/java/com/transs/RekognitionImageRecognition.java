package com.transs;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RekognitionImageRecognition implements ImageRecognition
{
    private static final Logger LOG = Logger.getLogger(RekognitionImageRecognition.class);
    private final static String TODO_STATUS = "ToDo";
    private final static String ALM_TODO_STATUS = "To Do";
    private final static String IN_PROGRESS_STATUS = "InProgress";
    private final static String ALM_IN_PROGRESS_STATUS = "In Progress";
    private final static String DONE_STATUS = "Done";
    private final static String ALM_DONE_STATUS = "Done";
    private final static String WORD = "Word";
    private double lineBetweenToDoAndInProgress;
    private double lineBetweenInProgressAndDone;

    @Override
    public Set<WorkItemDetails> analyzeImage(byte[] rawData)
    {
        LOG.info("Taldo: Starting to analyze image");
        Set<WorkItemDetails> workItemsDetails = new HashSet<>();
        try
        {
            LOG.info("Taldo: Getting the Rekognition Client");
            AmazonRekognition rekognitionClient = getAmazonRekognitionClient();
            LOG.info("Taldo: Got Rekognition Client");
            DetectTextRequest request = createDetectTextRequest(rawData);
            LOG.info("Taldo: Created Text Request");
            DetectTextResult detectTextResult = rekognitionClient.detectText(request);
            LOG.info("Taldo: Got Result");
            List<TextDetection> textDetections = detectTextResult.getTextDetections();
            LOG.info("Taldo: Total text detections: "  + textDetections.size());
            List<TextDetection> wordTextDetections = textDetections.stream()
                    .filter(detectedText -> detectedText.getType().equalsIgnoreCase(WORD)).collect(Collectors.toList());
            detectLinesBetweenStates(wordTextDetections);
            workItemsDetails = wordTextDetections.stream()
                    .map((TextDetection textDetection) -> new WorkItemDetails(getXCoordinate(textDetection, 0), getXCoordinate(textDetection, 1), textDetection.getDetectedText(), null))
                    .filter(text -> isWorkItem(text)).distinct()
                    .collect(Collectors.toSet());

            setWorkItemsNewStatus(workItemsDetails);

            printDetectedText(textDetections);
        }
        catch (AmazonRekognitionException e)
        {
            LOG.error("Taldo: Got Exception from rekognition", e);
        }
        return workItemsDetails;
    }

    private void setWorkItemsNewStatus(Set<WorkItemDetails> workItemsDetails)
    {
        for (WorkItemDetails workItemDetails : workItemsDetails)
        {
            if (workItemDetails.leftXLocation <= lineBetweenToDoAndInProgress)
            {
                workItemDetails.newState = ALM_TODO_STATUS;
            }
            else if (workItemDetails.leftXLocation > lineBetweenToDoAndInProgress && workItemDetails.leftXLocation <= lineBetweenInProgressAndDone)
            {
                workItemDetails.newState = ALM_IN_PROGRESS_STATUS;
            }
            else
            {
                workItemDetails.newState = ALM_DONE_STATUS;
            }
            LOG.info("Taldo: Work item found: " + workItemDetails.id + " " +workItemDetails.newState);
        }
    }

    private void detectLinesBetweenStates(List<TextDetection> wordTextDetections)
    {
        XCoordinates toDoCoordinates = getXCoordinates(wordTextDetections, TODO_STATUS);
        XCoordinates inProgressCoordinates = getXCoordinates(wordTextDetections, IN_PROGRESS_STATUS);
        XCoordinates doneCoordinates = getXCoordinates(wordTextDetections, DONE_STATUS);
        lineBetweenToDoAndInProgress = (toDoCoordinates.right + inProgressCoordinates.left) * 0.5;
        lineBetweenInProgressAndDone = (inProgressCoordinates.right + doneCoordinates.left) * 0.5;
    }

    private XCoordinates getXCoordinates(List<TextDetection> wordTextDetections, String status)
    {
        TextDetection textDetection = wordTextDetections.stream().filter(wordTextDetection -> wordTextDetection.getDetectedText().equalsIgnoreCase(status)).findFirst().get();
        Float left = textDetection.getGeometry().getPolygon().get(0).getX();
        Float right = textDetection.getGeometry().getPolygon().get(1).getX();
        return new XCoordinates(left, right);
    }

    private Float getXCoordinate(TextDetection textDetection, int index)
    {
        return textDetection.getGeometry().getPolygon().get(index).getX();
    }

    private boolean isWorkItem(WorkItemDetails workItemDetails)
    {
        String lowerCasedId = workItemDetails.id.toLowerCase();
        return !(lowerCasedId.startsWith(TODO_STATUS.toLowerCase()) || lowerCasedId.startsWith(IN_PROGRESS_STATUS.toLowerCase()) || lowerCasedId.startsWith(DONE_STATUS.toLowerCase()));
    }

    private void printDetectedText(List<TextDetection> textDetections)
    {
        for (TextDetection text : textDetections)
        {
            printResultReport(text);
        }
    }

    private DetectTextRequest createDetectTextRequest(byte[] rawData)
    {
        return new DetectTextRequest()
                .withImage(new Image()
                        .withBytes(ByteBuffer.wrap(rawData)));
    }


    private void printResultReport(TextDetection text)
    {
        System.out.println("Detected: " + text.getDetectedText());
        System.out.println("Confidence: " + text.getConfidence().toString());
        System.out.println("Id : " + text.getId());
        System.out.println("Parent Id: " + text.getParentId());
        System.out.println("Type: " + text.getType());
        System.out.println();
    }

    private AmazonRekognition getAmazonRekognitionClient()
    {
        return AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }
}
