package com.campusconnect.agent.dto;

import lombok.Data;

@Data
public class CampusTodoDTO {
    private String title;
    private Boolean done = false;
}