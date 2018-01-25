package com.transs;

public class WorkItemDetails
{
    public double leftXLocation;
    public double rightXLocation;
    public String id;
    public String newState;

    public WorkItemDetails(double leftXLocation, double rightXLocation, String id, String newState)
    {
        this.leftXLocation = leftXLocation;
        this.rightXLocation = rightXLocation;
        this.id = id;
        this.newState = newState;
    }
}
