package com.campusconnect.agent.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CampusChunkService {

    /**
     * 简单按字符切片，后面可以升级成按段落/标题切片
     */
    public List<String> split(String content) {
        List<String> chunks = new ArrayList<>();

        if (content == null || content.trim().isEmpty()) {
            return chunks;
        }

        String text = content.trim();

        int chunkSize = 600;
        int overlap = 80;

        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            String chunk = text.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= text.length()) {
                break;
            }

            start = end - overlap;
        }

        return chunks;
    }
}