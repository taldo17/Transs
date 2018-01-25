package com.transs;

import java.util.Set;

public interface ALMProvider
{
    void updateWorkItems(Set<WorkItemDetails> workItemDetails);
    String getWorkItemStatus(String id);
}
