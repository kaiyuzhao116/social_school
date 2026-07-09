package com.campusconnect.agent.service;

import com.campusconnect.agent.dto.CampusCrawlRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampusCrawlerScheduleService {

    private final CampusCrawlerService campusCrawlerService;

    /**
     * 自动导入渤海大学官网通知。
     *
     * 每天早上 8 点和晚上 8 点执行一次。
     * 旧通知会被 URL 去重自动跳过，只导入新通知。
     */
    @Scheduled(cron = "0 0 8,20 * * ?", zone = "Asia/Shanghai")
    public void autoImportBhuNotices() {
        log.info("开始执行渤海大学官网通知自动导入任务");

        CampusCrawlRequest request = buildBhuNoticeRequest();

        try {
            Map<String, Object> result = campusCrawlerService.crawlAndImport(request);

            log.info("渤海大学官网通知自动导入完成：totalScanned={}, successCount={}, failCount={}",
                    result.get("totalScanned"),
                    result.get("successCount"),
                    result.get("failCount"));

            log.info("自动导入详细结果：{}", result);
        } catch (Exception e) {
            log.error("渤海大学官网通知自动导入失败", e);
        }
    }

    /**
     * 方便后面手动测试复用。
     */
    public Map<String, Object> manualImportBhuNotices() {
        log.info("手动触发渤海大学官网通知导入任务");

        CampusCrawlRequest request = buildBhuNoticeRequest();

        return campusCrawlerService.crawlAndImport(request);
    }

    private CampusCrawlRequest buildBhuNoticeRequest() {
        CampusCrawlRequest request = new CampusCrawlRequest();

        request.setListUrl("https://www.bhu.edu.cn/engine2/general/293065/type/more-datas");
        request.setSourceName("渤海大学官网");
        request.setSourceType("通知公告");
        request.setTrustLevel("高");

        // 每次最多扫描 10 条，避免定时任务太重
        request.setMaxCount(10);

        request.setEngineInstanceId(425304L);
        request.setPageNum(1);
        request.setPageSize(20);
        request.setTypeId(2613739L);
        request.setSw("");

        request.setPageId(55965L);
        request.setWebsiteId(43901L);

        return request;
    }
}