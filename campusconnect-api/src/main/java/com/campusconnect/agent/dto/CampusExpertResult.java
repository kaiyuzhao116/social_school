package com.campusconnect.agent.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CampusExpertResult {

    private CampusAgentResponse response;

    private List<CampusRagChunkDTO> chunks = new ArrayList<>();
}