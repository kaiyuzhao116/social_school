package com.campusconnect.agent.dto;

import lombok.Data;

@Data
public class CampusCrawlRequest {

    /**
     * 列表页 URL 或 more-datas 接口 URL
     */
    private String listUrl;

    /**
     * 来源名称，比如：渤海大学教务处
     */
    private String sourceName;

    /**
     * 来源类型，比如：教务处 / 就业网 / 学院通知
     */
    private String sourceType;

    /**
     * 可信度，默认高
     */
    private String trustLevel;

    /**
     * 最多导入多少条
     */
    private Integer maxCount = 10;

    /**
     * 渤海大学 more-datas 接口参数
     */
    private Long engineInstanceId;

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    private Long typeId;

    private String sw;

    private Long pageId;

    private Long websiteId;
}