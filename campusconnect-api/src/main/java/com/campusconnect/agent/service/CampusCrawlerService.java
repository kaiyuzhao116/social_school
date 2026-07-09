package com.campusconnect.agent.service;

import com.campusconnect.agent.dto.CampusCrawlRequest;
import com.campusconnect.agent.dto.CampusKnowledgeImportRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampusCrawlerService {

    private final CampusKnowledgeImportService campusKnowledgeImportService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> crawlAndImport(CampusCrawlRequest request) {
        if (isBlank(request.getListUrl())) {
            throw new RuntimeException("列表页 URL 不能为空");
        }

        // 渤海大学动态接口：直接请求 more-datas JSON
        if (request.getListUrl().contains("/type/more-datas")) {
            return crawlBhuMoreDatasApi(request);
        }

        // 普通静态 HTML 页面：从 a[href] 里提取详情页
        return crawlStaticHtml(request);
    }

    /**
     * 渤海大学 more-datas 接口爬取
     */
    private Map<String, Object> crawlBhuMoreDatasApi(CampusCrawlRequest request) {
        if (request.getEngineInstanceId() == null) {
            throw new RuntimeException("engineInstanceId 不能为空");
        }

        if (request.getTypeId() == null) {
            throw new RuntimeException("typeId 不能为空");
        }

        int maxCount = request.getMaxCount() == null ? 10 : request.getMaxCount();
        maxCount = Math.min(maxCount, 20);

        int pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null ? 20 : request.getPageSize();

        int total = 0;
        int success = 0;
        int fail = 0;

        List<String> successUrls = new ArrayList<>();
        List<String> failMessages = new ArrayList<>();

        try {
            Connection.Response response = Jsoup.connect(request.getListUrl())
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 CampusConnectBot/1.0")
                    .timeout(10000)
                    .method(Connection.Method.POST)
                    .data("engineInstanceId", String.valueOf(request.getEngineInstanceId()))
                    .data("pageNum", String.valueOf(pageNum))
                    .data("pageSize", String.valueOf(pageSize))
                    .data("typeId", String.valueOf(request.getTypeId()))
                    .data("sw", request.getSw() == null ? "" : request.getSw())
                    .execute();

            String body = response.body();
            JsonNode root = objectMapper.readTree(body);

            List<JsonNode> items = new ArrayList<>();
            collectNoticeItems(root, items);

            log.info("渤大 more-datas 通知数量：{}", items.size());

            Set<String> visitedUrls = new LinkedHashSet<>();

            for (JsonNode item : items) {
                // 注意：这里用 total 控制扫描数量，不用 success。
                // 因为重复通知会 imported=false，不算 success。
                if (total >= maxCount) {
                    break;
                }

                String title = extractTitleFromJson(item);
                String rawUrl = item.path("url").asText();

                if (isBlank(rawUrl)) {
                    continue;
                }

                String detailUrl = buildBhuDetailUrl(request, rawUrl);

                if (visitedUrls.contains(detailUrl)) {
                    continue;
                }

                visitedUrls.add(detailUrl);
                total++;

                try {
                    Thread.sleep(300);

                    Document detailDoc = Jsoup.connect(detailUrl)
                            .ignoreContentType(true)
                            .userAgent("Mozilla/5.0 CampusConnectBot/1.0")
                            .timeout(10000)
                            .get();

                    String realTitle = extractTitle(detailDoc, title);
                    String content = extractContent(detailDoc);

                    if (content.length() < 80) {
                        fail++;
                        String msg = "正文太短，跳过：" + realTitle + "，地址：" + detailUrl;
                        failMessages.add(msg);
                        log.warn(msg);
                        continue;
                    }

                    CampusKnowledgeImportRequest importRequest = new CampusKnowledgeImportRequest();
                    importRequest.setTitle(realTitle);
                    importRequest.setSourceName(defaultValue(request.getSourceName(), "渤海大学"));
                    importRequest.setSourceType(defaultValue(request.getSourceType(), "校园通知"));
                    importRequest.setTrustLevel(defaultValue(request.getTrustLevel(), "高"));
                    importRequest.setUrl(detailUrl);
                    importRequest.setContent(content);

                    Map<String, Object> importResult =
                            campusKnowledgeImportService.importKnowledge(importRequest);

                    Boolean imported = (Boolean) importResult.get("imported");

                    if (Boolean.TRUE.equals(imported)) {
                        success++;
                        successUrls.add(detailUrl);

                        log.info("校园资料导入成功：{} {}", realTitle, detailUrl);
                    } else {
                        log.info("资料已存在，跳过重复导入：{}，原因：{}",
                                detailUrl,
                                importResult.get("skipReason"));
                    }
                } catch (Exception e) {
                    fail++;
                    String msg = "导入失败：" + detailUrl + "，原因：" + e.getMessage();
                    failMessages.add(msg);
                    log.warn(msg);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("爬取渤大 more-datas 接口失败：" + e.getMessage(), e);
        }

        return buildResult(request.getListUrl(), total, success, fail, successUrls, failMessages);
    }

    /**
     * 普通静态 HTML 页面爬取
     */
    private Map<String, Object> crawlStaticHtml(CampusCrawlRequest request) {
        int maxCount = request.getMaxCount() == null ? 10 : request.getMaxCount();
        maxCount = Math.min(maxCount, 20);

        int total = 0;
        int success = 0;
        int fail = 0;

        List<String> successUrls = new ArrayList<>();
        List<String> failMessages = new ArrayList<>();

        try {
            Document listDoc = Jsoup.connect(request.getListUrl())
                    .userAgent("Mozilla/5.0 CampusConnectBot/1.0")
                    .timeout(10000)
                    .get();

            List<String> detailUrls = extractDetailUrls(listDoc, request.getListUrl());

            log.info("列表页候选详情链接数量：{}", detailUrls.size());

            for (String detailUrl : detailUrls) {
                if (total >= maxCount) {
                    break;
                }

                total++;

                try {
                    Thread.sleep(300);

                    Document detailDoc = Jsoup.connect(detailUrl)
                            .userAgent("Mozilla/5.0 CampusConnectBot/1.0")
                            .timeout(10000)
                            .get();

                    String realTitle = extractTitle(detailDoc, "校园通知");
                    String content = extractContent(detailDoc);

                    if (content.length() < 80) {
                        fail++;
                        String msg = "正文太短，跳过：" + realTitle + "，地址：" + detailUrl;
                        failMessages.add(msg);
                        log.warn(msg);
                        continue;
                    }

                    CampusKnowledgeImportRequest importRequest = new CampusKnowledgeImportRequest();
                    importRequest.setTitle(realTitle);
                    importRequest.setSourceName(defaultValue(request.getSourceName(), "渤海大学"));
                    importRequest.setSourceType(defaultValue(request.getSourceType(), "校园通知"));
                    importRequest.setTrustLevel(defaultValue(request.getTrustLevel(), "高"));
                    importRequest.setUrl(detailUrl);
                    importRequest.setContent(content);

                    Map<String, Object> importResult =
                            campusKnowledgeImportService.importKnowledge(importRequest);

                    Boolean imported = (Boolean) importResult.get("imported");

                    if (Boolean.TRUE.equals(imported)) {
                        success++;
                        successUrls.add(detailUrl);

                        log.info("校园资料导入成功：{} {}", realTitle, detailUrl);
                    } else {
                        log.info("资料已存在，跳过重复导入：{}，原因：{}",
                                detailUrl,
                                importResult.get("skipReason"));
                    }
                } catch (Exception e) {
                    fail++;
                    String msg = "导入失败：" + detailUrl + "，原因：" + e.getMessage();
                    failMessages.add(msg);
                    log.warn(msg);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("爬取列表页失败：" + e.getMessage(), e);
        }

        return buildResult(request.getListUrl(), total, success, fail, successUrls, failMessages);
    }

    private String buildBhuDetailUrl(CampusCrawlRequest request, String rawUrl) {
        if (isBlank(rawUrl)) {
            return "";
        }

        Pattern pattern = Pattern.compile("/engine2/general-rest/(\\d+)/proxy-detail-url");
        Matcher matcher = pattern.matcher(rawUrl);

        if (matcher.find()) {
            String articleId = matcher.group(1);

            Long pageId = request.getPageId() == null ? 55965L : request.getPageId();
            Long websiteId = request.getWebsiteId() == null ? 43901L : request.getWebsiteId();

            return "https://www.bhu.edu.cn/engine2/general/"
                    + articleId
                    + "/detail?engineInstanceId="
                    + request.getEngineInstanceId()
                    + "&typeId="
                    + request.getTypeId()
                    + "&pageId="
                    + pageId
                    + "&websiteId="
                    + websiteId
                    + "&currentBranch=0";
        }

        return toAbsoluteUrl(request.getListUrl(), rawUrl);
    }

    /**
     * 从 more-datas 返回的 JSON 里递归找通知项
     */
    private void collectNoticeItems(JsonNode node, List<JsonNode> items) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            String url = node.path("url").asText();

            if (!isBlank(url) && url.contains("proxy-detail-url")) {
                items.add(node);
                return;
            }

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                collectNoticeItems(entry.getValue(), items);
            }
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                collectNoticeItems(child, items);
            }
        }
    }

    private String extractTitleFromJson(JsonNode item) {
        String title = findTitleValue(item);

        if (!isBlank(title)) {
            return title;
        }

        String typeName = item.path("typeName").asText();
        if (!isBlank(typeName)) {
            return typeName;
        }

        return "校园通知";
    }

    private String findTitleValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }

        if (node.isObject()) {
            String key = node.path("key").asText();
            String value = node.path("value").asText();

            if ("标题".equals(key) && !isBlank(value)) {
                return cleanText(value);
            }

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String result = findTitleValue(entry.getValue());
                if (!isBlank(result)) {
                    return result;
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                String result = findTitleValue(child);
                if (!isBlank(result)) {
                    return result;
                }
            }
        }

        return "";
    }

    /**
     * 从静态列表页提取详情页链接
     */
    private List<String> extractDetailUrls(Document listDoc, String listUrl) {
        Set<String> urls = new LinkedHashSet<>();

        Elements links = listDoc.select("a[href]");
        for (Element link : links) {
            String detailUrl = link.absUrl("href");

            if (isValidDetailUrl(listUrl, detailUrl)) {
                urls.add(detailUrl);
            }
        }

        String html = listDoc.html().replace("&amp;", "&");

        Pattern pattern = Pattern.compile(
                "(https?://[^\"'<>\\s]+/engine2/general/\\d+/detail\\?[^\"'<>\\s]+)|" +
                        "(/engine2/general/\\d+/detail\\?[^\"'<>\\s]+)"
        );

        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String rawUrl = matcher.group();
            String detailUrl = toAbsoluteUrl(listUrl, rawUrl);

            if (isValidDetailUrl(listUrl, detailUrl)) {
                urls.add(detailUrl);
            }
        }

        return new ArrayList<>(urls);
    }

    private boolean isValidDetailUrl(String listUrl, String detailUrl) {
        if (isBlank(detailUrl)) {
            return false;
        }

        if (!isSameHost(listUrl, detailUrl)) {
            return false;
        }

        if (!(detailUrl.contains("/detail") || detailUrl.contains("detail?"))) {
            return false;
        }

        if (detailUrl.contains("/more?")) {
            return false;
        }

        String lower = detailUrl.toLowerCase();

        return !(lower.endsWith(".jpg")
                || lower.endsWith(".png")
                || lower.endsWith(".gif")
                || lower.endsWith(".css")
                || lower.endsWith(".js")
                || lower.endsWith(".pdf"));
    }

    private String extractTitle(Document doc, String fallback) {
        String h1 = cleanText(doc.select("h1").text());
        if (!isBlank(h1)) {
            return h1;
        }

        String titleClass = cleanText(doc.select(".title, .article-title, .news-title, .detail-title").text());
        if (!isBlank(titleClass)) {
            return titleClass;
        }

        String metaTitle = cleanText(doc.select("meta[name=ArticleTitle]").attr("content"));
        if (!isBlank(metaTitle)) {
            return metaTitle;
        }

        String title = cleanText(doc.title());
        if (!isBlank(title)) {
            return title;
        }

        return fallback;
    }

    private String extractContent(Document doc) {
        doc.select("script, style, nav, header, footer").remove();

        String[] selectors = {
                "article",
                ".article",
                ".content",
                ".news-content",
                ".detail",
                ".main-content",
                ".wp_articlecontent",
                ".v_news_content",
                "#vsb_content",
                "#vsb_content_2",
                ".TRS_Editor",
                ".con",
                ".info-content",
                ".article_content",
                ".news_content",
                "#content"
        };

        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            String text = cleanText(elements.text());

            if (text.length() > 100) {
                return text;
            }
        }

        return cleanText(doc.body().text());
    }

    private String toAbsoluteUrl(String listUrl, String rawUrl) {
        try {
            if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                return rawUrl;
            }

            URI baseUri = URI.create(listUrl);

            if (rawUrl.startsWith("/")) {
                return baseUri.getScheme() + "://" + baseUri.getHost() + rawUrl;
            }

            return baseUri.resolve(rawUrl).toString();
        } catch (Exception e) {
            return rawUrl;
        }
    }

    private boolean isSameHost(String listUrl, String detailUrl) {
        try {
            URI listUri = URI.create(listUrl);
            URI detailUri = URI.create(detailUrl);

            String listHost = listUri.getHost();
            String detailHost = detailUri.getHost();

            return listHost != null && listHost.equalsIgnoreCase(detailHost);
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> buildResult(
            String listUrl,
            int total,
            int success,
            int fail,
            List<String> successUrls,
            List<String> failMessages
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("listUrl", listUrl);
        result.put("totalScanned", total);
        result.put("successCount", success);
        result.put("failCount", fail);
        result.put("successUrls", successUrls);
        result.put("failMessages", failMessages);
        return result;
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

    private String defaultValue(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}