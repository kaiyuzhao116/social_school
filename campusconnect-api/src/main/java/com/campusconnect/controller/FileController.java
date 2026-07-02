package com.campusconnect.controller;

import com.campusconnect.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * 文件上传控制器
 * 统一处理所有文件上传请求
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 通用文件上传接口 (/file/upload)
     * 用于头像、封面等
     */
    @PostMapping("/file/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "image") String type) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }

        // 检查文件大小 (最大5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }

        try {
            String subDir = switch (type) {
                case "avatar" -> "avatars";
                case "cover" -> "covers";
                default -> "images";
            };
            String url = saveFile(file, subDir, false);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 单图片上传接口 (/upload/image)
     */
    @PostMapping("/upload/image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }

        try {
            String url = saveFile(file, "images", true);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success(data);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 多图片上传接口 (/upload/images)
     */
    @PostMapping("/upload/images")
    public Result<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择文件");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String url = saveFile(file, "images", true);
                    urls.add(url);
                } catch (IOException e) {
                    log.warn("文件上传失败: {}", file.getOriginalFilename());
                }
            }
        }

        Map<String, List<String>> data = new HashMap<>();
        data.put("urls", urls);
        return Result.success(data);
    }

    /**
     * 通用文件上传接口（非图片）(/upload/file)
     */
    @PostMapping("/upload/file")
    public Result<?> uploadGeneralFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        try {
            String url = saveFile(file, "files", true);
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("name", file.getOriginalFilename());
            data.put("size", file.getSize());
            return Result.success(data);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 保存文件到磁盘
     * 
     * @param file    上传的文件
     * @param subDir  子目录
     * @param useDate 是否按日期分目录
     * @return 访问URL
     */
    private String saveFile(MultipartFile file, String subDir, boolean useDate) throws IOException {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // 构建目录路径
        Path dirPath;
        String urlPath;
        if (useDate) {
            String dateDir = LocalDate.now().toString();
            dirPath = Paths.get(uploadPath, subDir, dateDir);
            urlPath = urlPrefix + "/" + subDir + "/" + dateDir + "/" + filename;
        } else {
            dirPath = Paths.get(uploadPath, subDir);
            urlPath = urlPrefix + "/" + subDir + "/" + filename;
        }

        // 创建目录
        Files.createDirectories(dirPath);

        // 保存文件
        Path filePath = dirPath.resolve(filename);
        file.transferTo(filePath.toAbsolutePath().toFile());

        return urlPath;
    }
}
