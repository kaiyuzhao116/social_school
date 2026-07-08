package com.campusconnect.agent.controller;

import com.campusconnect.agent.dto.GroupBuyAgentRequest;
import com.campusconnect.agent.dto.GroupBuyDraftDTO;
import com.campusconnect.agent.service.GroupBuyAgentService;
import com.campusconnect.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/group-buy")
@RequiredArgsConstructor
public class GroupBuyAgentController {

    private final GroupBuyAgentService groupBuyAgentService;

    @PostMapping("/draft")
    public Result<GroupBuyDraftDTO> generateDraft(@RequestBody GroupBuyAgentRequest request) {
        return Result.success(groupBuyAgentService.generateDraft(request.getContent()));
    }
}