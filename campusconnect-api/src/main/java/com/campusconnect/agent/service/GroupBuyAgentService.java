package com.campusconnect.agent.service;

import com.campusconnect.agent.dto.GroupBuyDraftDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyAgentService {

    private final GroupBuyLlmClient groupBuyLlmClient;

    public GroupBuyDraftDTO generateDraft(String content) {
        try {
            GroupBuyDraftDTO dto = groupBuyLlmClient.generateDraftByLlm(content);
            log.info("大模型生成拼团草稿成功：{}", dto);
            return normalize(dto, content);
        } catch (Exception e) {
            log.warn("大模型生成失败，降级为规则解析：{}", e.getMessage());
            return generateByRules(content);
        }
    }

    private GroupBuyDraftDTO normalize(GroupBuyDraftDTO dto, String content) {
        if (dto == null) {
            return generateByRules(content);
        }

        if (isBlank(dto.getTitle())) {
            dto.setTitle("校园拼团");
        }

        if (isBlank(dto.getCategory())) {
            dto.setCategory("其他");
        }

        if (dto.getTargetCount() == null || dto.getTargetCount() < 2) {
            dto.setTargetCount(2);
        }

        if (dto.getTargetCount() > 20) {
            dto.setTargetCount(20);
        }

        if (isBlank(dto.getLocation())) {
            dto.setLocation("校内");
        }

        if (isBlank(dto.getStartTimeText())) {
            dto.setStartTimeText("近期");
        }

        if (isBlank(dto.getDescription())) {
            dto.setDescription(content + "。有需要的同学可以一起加入。");
        }

        return dto;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 大模型失败时，走规则解析兜底
     */
    private GroupBuyDraftDTO generateByRules(String content) {
        GroupBuyDraftDTO dto = new GroupBuyDraftDTO();

        String category = guessCategory(content);
        String location = guessLocation(content);
        Integer targetCount = guessTargetCount(content);
        String timeText = guessTimeText(content);

        dto.setCategory(category);
        dto.setTargetCount(targetCount);
        dto.setLocation(location);
        dto.setStartTimeText(timeText);

        dto.setTitle(buildTitle(content, timeText, category, location));
        dto.setDescription(buildDescription(content, targetCount, category, location));

        return dto;
    }

    private String guessCategory(String content) {
        if (content.contains("拼车") || content.contains("高铁站") || content.contains("锦州南站") || content.contains("车站")) {
            return "拼车";
        }

        if (content.contains("外卖") || content.contains("奶茶") || content.contains("烧烤") || content.contains("点餐")) {
            return "拼饭";
        }

        if (content.contains("水果") || content.contains("团购") || content.contains("一起买")) {
            return "团购";
        }

        if (content.contains("打球") || content.contains("羽毛球") || content.contains("篮球")) {
            return "运动";
        }

        return "其他";
    }

    private String guessLocation(String content) {
        if (content.contains("锦州南站")) {
            return "锦州南站";
        }

        if (content.contains("高铁站")) {
            return "高铁站";
        }

        if (content.contains("食堂")) {
            return "食堂";
        }

        if (content.contains("操场")) {
            return "操场";
        }

        if (content.contains("体育馆")) {
            return "体育馆";
        }

        Pattern pattern = Pattern.compile("去(.+?)(，|。|\\s|$)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "校内";
    }

    private Integer guessTargetCount(String content) {
        Pattern pattern = Pattern.compile("找\\s*(\\d+)\\s*个人");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            int needCount = Integer.parseInt(matcher.group(1));
            return needCount + 1;
        }

        return 2;
    }

    private String guessTimeText(String content) {
        if (content.contains("明天下午")) {
            return "明天下午";
        }

        if (content.contains("明天上午")) {
            return "明天上午";
        }

        if (content.contains("今晚") || content.contains("今天晚上")) {
            return "今晚";
        }

        if (content.contains("周六")) {
            return "周六";
        }

        if (content.contains("周日")) {
            return "周日";
        }

        return "近期";
    }

    private String buildTitle(String content, String timeText, String category, String location) {
        if (content.contains("烧烤")) {
            return timeText + "烧烤外卖拼单";
        }

        if (content.contains("奶茶")) {
            return timeText + "奶茶拼单";
        }

        if (content.contains("咖啡") || content.contains("瑞幸")) {
            return timeText + "咖啡拼单";
        }

        if (content.contains("水果")) {
            return timeText + "水果团购";
        }

        if (content.contains("打印")) {
            return timeText + "打印拼单";
        }

        if ("拼车".equals(category)) {
            return timeText + "拼车去" + location;
        }

        if ("运动".equals(category)) {
            return timeText + "一起运动";
        }

        if ("拼饭".equals(category)) {
            return timeText + "一起拼饭";
        }

        if ("团购".equals(category)) {
            return timeText + "一起团购";
        }

        return timeText + "校园拼团";
    }

    private String buildDescription(String content, Integer targetCount, String category, String location) {
        return content + "。目标人数 " + targetCount + " 人，地点：" + location + "。有需要的同学可以一起加入。";
    }
}