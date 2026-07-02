package com.campusconnect.ws;

import com.campusconnect.entity.GroupBuy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyLivePushService {

    private final GroupBuyLiveWebSocketHandler groupBuyLiveWebSocketHandler;
    private final ObjectMapper objectMapper;
    /**
     * 推送：有人发起拼团
     */
    public void pushCreated(GroupBuy groupBuy) {
        if (groupBuy == null) {
            return;
        }

        String message = "📢 新拼团发布：《" + groupBuy.getTitle()
                + "》，目标人数 "
                + groupBuy.getTargetCount()
                + " 人，快来一起参加吧";

        sendGroupBuyMessage(
                "GROUP_BUY_CREATED",
                groupBuy,
                message
        );
    }

    /**
     * 推送：有人加入拼团
     */
    public void pushJoined(GroupBuy groupBuy) {
        if (groupBuy == null) {
            return;
        }

        String message = "👥 有人加入了《" + groupBuy.getTitle()
                + "》，当前人数 "
                + groupBuy.getCurrentCount()
                + " / "
                + groupBuy.getTargetCount();

        sendGroupBuyMessage(
                "GROUP_BUY_JOINED",
                groupBuy,
                message
        );
    }

    /**
     * 推送：拼团已成团
     */
    public void pushSuccess(GroupBuy groupBuy) {
        if (groupBuy == null) {
            return;
        }

        String message = "🎉 《" + groupBuy.getTitle()
                + "》已成功成团，当前人数 "
                + groupBuy.getCurrentCount()
                + " / "
                + groupBuy.getTargetCount()
                + "，请及时联系队友完成拼单";

        sendGroupBuyMessage(
                "GROUP_BUY_SUCCESS",
                groupBuy,
                message
        );
    }

    /**
     * 推送：拼团已过期
     */
    public void pushExpired(GroupBuy groupBuy) {
        if (groupBuy == null) {
            return;
        }

        String message = "⏰ 《" + groupBuy.getTitle()
                + "》已过期未成团，系统已自动结束该拼团";

        sendGroupBuyMessage(
                "GROUP_BUY_EXPIRED",
                groupBuy,
                message
        );
    }

    private void sendGroupBuyMessage(String type, GroupBuy groupBuy, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();

            payload.put("type", type);
            payload.put("groupBuyId", groupBuy.getId());
            payload.put("title", groupBuy.getTitle());
            payload.put("message", message);
            payload.put("currentCount", groupBuy.getCurrentCount());
            payload.put("targetCount", groupBuy.getTargetCount());
            payload.put("status", groupBuy.getStatus());

            String json = objectMapper.writeValueAsString(payload);

            groupBuyLiveWebSocketHandler.sendToAll(json);

            log.info("【拼团实时推送】已推送实时卡片消息，type={}, groupBuyId={}, 在线连接数={}",
                    type,
                    groupBuy.getId(),
                    groupBuyLiveWebSocketHandler.getOnlineCount()
            );
        } catch (Exception e) {
            log.error("【拼团实时推送】推送消息失败，type={}, groupBuyId={}",
                    type,
                    groupBuy.getId(),
                    e
            );
        }
    }
}