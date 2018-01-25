package com.transs;

import java.util.Set;

public interface ImageRecognition
{
    Set<WorkItemDetails> analyzeImage(byte[] rawData);
}
