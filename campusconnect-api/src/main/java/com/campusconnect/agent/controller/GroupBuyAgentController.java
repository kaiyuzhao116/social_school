package com.campusconnect.agent.controller;

import com.campusconnect.agent.dto.GroupBuyAgentRequest;
import com.campusconnect.agent.dto.GroupBuyDraftDTO;
import com.campusconnect.agent.service.AgentRateLimitService;
import com.campusconnect.agent.service.GroupBuyAgentService;
import com.campusconnect.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/group-buy")
@RequiredArgsConstructor
public class GroupBuyAgentController {

    private final GroupBuyAgentService groupBuyAgentService;
    private final AgentRateLimitService agentRateLimitService;

    @PostMapping("/draft")
    public Result<GroupBuyDraftDTO> generateDraft(
            @RequestBody GroupBuyAgentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String clientIp = getClientIp(httpServletRequest);

        String rateLimitKey = "agent:group-buy:draft:ip:" + clientIp;


        // 1 分钟最多 1 次
        agentRateLimitService.checkLimit(rateLimitKey, 1, 60);
        return Result.success(groupBuyAgentService.generateDraft(request.getContent()));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}