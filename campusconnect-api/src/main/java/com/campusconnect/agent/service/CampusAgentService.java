package com.campusconnect.agent.service;

import com.campusconnect.agent.dto.CampusAgentRequest;
import com.campusconnect.agent.dto.CampusAgentResponse;
import com.campusconnect.agent.orchestrator.CampusAgentOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampusAgentService {

    private final CampusAgentOrchestrator campusAgentOrchestrator;

    public CampusAgentResponse chat(CampusAgentRequest request) {
        return campusAgentOrchestrator.chat(request);
    }
}