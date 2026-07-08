package com.campusconnect.agent.dto;

import lombok.Data;

@Data
public class GroupBuyDraftDTO {
    private String title;
    private String category;
    private Integer targetCount;
    private String location;
    private String startTimeText;
    private String description;
}