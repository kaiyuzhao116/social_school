package com.campusconnect.agent.expert;

import org.springframework.stereotype.Service;

@Service
public class CampusRouterExpert {

    public String route(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "GENERAL_QA";
        }

        if (question.contains("最近")
                || question.contains("最新")
                || question.contains("通知")
                || question.contains("公告")) {
            return "NOTICE_QUERY";
        }

        if (question.contains("怎么申请")
                || question.contains("怎么办")
                || question.contains("流程")
                || question.contains("材料")
                || question.contains("去哪办")) {
            return "SERVICE_GUIDE";
        }

        if (question.contains("截止")
                || question.contains("什么时候")
                || question.contains("时间")) {
            return "DEADLINE_QUERY";
        }

        if (question.contains("和我有关")
                || question.contains("跟我有关")
                || question.contains("和我相关")
                || question.contains("跟我相关")
                || question.contains("相关吗")
                || question.contains("有我相关")
                || question.contains("有和我相关")
                || question.contains("我需要")
                || question.contains("我能参加")
                || question.contains("适合我")
                || question.contains("是否相关")) {
            return "RELATED_CHECK";
        }

        if (question.contains("总结")
                || question.contains("三句话")
                || question.contains("看懂")) {
            return "NOTICE_SUMMARY";
        }

        return "GENERAL_QA";
    }
}