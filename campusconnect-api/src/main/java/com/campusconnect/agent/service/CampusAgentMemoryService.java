package com.campusconnect.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusconnect.agent.entity.CampusAgentMemory;
import com.campusconnect.agent.mapper.CampusAgentMemoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusAgentMemoryService {

    private final StringRedisTemplate stringRedisTemplate;
    private final CampusAgentMemoryMapper campusAgentMemoryMapper;

    private static final String RECENT_QUESTION_KEY_PREFIX = "campus:agent:recent:questions:";
    private static final int MAX_RECENT_QUESTIONS = 10;
    private static final Duration RECENT_QUESTION_TTL = Duration.ofDays(7);

    /**
     * 保存最近提问，只保留最近 10 条。
     * 不保存回答，避免 token 无限膨胀。
     */
    public void saveRecentQuestion(Long userId, String question) {
        if (userId == null || isBlank(question)) {
            return;
        }

        String key = buildRecentQuestionKey(userId);
        String cleanQuestion = limit(cleanText(question), 120);

        stringRedisTemplate.opsForList().leftPush(key, cleanQuestion);
        stringRedisTemplate.opsForList().trim(key, 0, MAX_RECENT_QUESTIONS - 1);
        stringRedisTemplate.expire(key, RECENT_QUESTION_TTL);
    }

    /**
     * 获取最近 10 次提问，按时间从旧到新返回。
     */
    public List<String> getRecentQuestions(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        String key = buildRecentQuestionKey(userId);

        List<String> questions = stringRedisTemplate.opsForList()
                .range(key, 0, MAX_RECENT_QUESTIONS - 1);

        if (questions == null || questions.isEmpty()) {
            return new ArrayList<>();
        }

        Collections.reverse(questions);
        return questions;
    }

    /**
     * 构建给大模型看的记忆上下文。
     * 这里严格限制长度，防止 token 爆炸。
     */
    public String buildMemoryContext(Long userId) {
        if (userId == null) {
            return "";
        }

        List<String> recentQuestions = getRecentQuestions(userId);
        List<CampusAgentMemory> longTermMemories = getLongTermMemories(userId, 10);

        StringBuilder sb = new StringBuilder();

        if (!recentQuestions.isEmpty()) {
            sb.append("【最近提问，仅供理解上下文，不代表事实】\n");

            for (int i = 0; i < recentQuestions.size(); i++) {
                sb.append(i + 1)
                        .append(". ")
                        .append(limit(recentQuestions.get(i), 80))
                        .append("\n");
            }
        }

        if (!longTermMemories.isEmpty()) {
            sb.append("\n【长期记忆，仅在和当前问题相关时使用】\n");

            for (CampusAgentMemory memory : longTermMemories) {
                sb.append("- ")
                        .append(limit(memory.getMemoryValue(), 120))
                        .append("\n");
            }
        }

        return limit(sb.toString(), 1200);
    }

    /**
     * 根据用户问题，判断是否需要保存长期记忆。
     * 只在用户明显表达长期信息时保存，避免乱存。
     */
    public void maybeSaveLongTermMemory(Long userId, String question) {
        if (userId == null || isBlank(question)) {
            return;
        }

        String cleanQuestion = cleanText(question);

        if (!shouldSaveLongTerm(cleanQuestion)) {
            return;
        }

        String memoryType = detectMemoryType(cleanQuestion);
        String memoryValue = cleanQuestion;
        String memoryKey = memoryType + ":" + md5(memoryValue);

        Long count = campusAgentMemoryMapper.selectCount(
                new LambdaQueryWrapper<CampusAgentMemory>()
                        .eq(CampusAgentMemory::getUserId, userId)
                        .eq(CampusAgentMemory::getMemoryKey, memoryKey)
        );

        if (count != null && count > 0) {
            return;
        }

        CampusAgentMemory memory = new CampusAgentMemory();
        memory.setUserId(userId);
        memory.setMemoryKey(memoryKey);
        memory.setMemoryValue(memoryValue);
        memory.setMemoryType(memoryType);
        memory.setSource("USER");
        memory.setStatus(1);
        memory.setCreatedAt(LocalDateTime.now());
        memory.setUpdatedAt(LocalDateTime.now());

        campusAgentMemoryMapper.insert(memory);
    }

    public List<CampusAgentMemory> getLongTermMemories(Long userId, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }

        int safeLimit = Math.min(Math.max(limit, 1), 20);

        return campusAgentMemoryMapper.selectList(
                new LambdaQueryWrapper<CampusAgentMemory>()
                        .eq(CampusAgentMemory::getUserId, userId)
                        .eq(CampusAgentMemory::getStatus, 1)
                        .orderByDesc(CampusAgentMemory::getUpdatedAt)
                        .last("LIMIT " + safeLimit)
        );
    }

    private boolean shouldSaveLongTerm(String question) {
        return question.contains("记住")
                || question.contains("以后")
                || question.contains("我是")
                || question.contains("我属于")
                || question.contains("我的学院")
                || question.contains("我的专业")
                || question.contains("我关注")
                || question.contains("我主要关心");
    }

    private String detectMemoryType(String question) {
        if (question.contains("我的专业")
                || question.contains("我的学院")
                || question.contains("我是")
                || question.contains("我属于")) {
            return "PROFILE";
        }

        if (question.contains("我关注")
                || question.contains("我主要关心")
                || question.contains("以后")) {
            return "PREFERENCE";
        }

        return "NOTE";
    }

    private String buildRecentQuestionKey(Long userId) {
        return RECENT_QUESTION_KEY_PREFIX + userId;
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String limit(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String md5(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            return String.valueOf(text.hashCode());
        }
    }
}