package com.transs;

import java.util.Set;

public interface ALMProvider
{
    void updateWorkItems(Set<WorkItemDetails> workItemDetails, String boardId, String token);
    String getWorkItemStatus(String id, String boardId, String token);
}
