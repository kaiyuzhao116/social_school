package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.ws.GroupBuyLiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group-buy-live")
@RequiredArgsConstructor
public class GroupBuyLiveController {

    private final GroupBuyLiveWebSocketHandler groupBuyLiveWebSocketHandler;

    /**
     * 测试 WebSocket 推送
     */
    @PostMapping("/test")
    public Result<?> testPush() {
        String message = """
                {
                  "type": "GROUP_BUY_SUCCESS",
                  "title": "迷你慕斯蛋糕拼团",
                  "message": "🎉 拼团已成团，当前人数 3 / 3，请及时联系队友完成拼单",
                  "currentCount": 3,
                  "targetCount": 3
                }
                """;

        groupBuyLiveWebSocketHandler.sendToAll(message);

        return Result.success("推送成功，当前在线连接数：" + groupBuyLiveWebSocketHandler.getOnlineCount());
    }
}