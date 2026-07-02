package com.campusconnect.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 临时数据库迁移 Runner
 * 用于补齐 conversation_member 表缺失的字段
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查并修复数据库字段...");

        try {
            // 尝试添加 last_read_at 字段
            try {
                jdbcTemplate.execute("ALTER TABLE conversation_member ADD COLUMN last_read_at DATETIME NULL");
                log.info("成功添加 conversation_member.last_read_at 字段");
            } catch (Exception e) {
                log.info("last_read_at 字段可能已存在或添加失败: {}", e.getMessage());
            }

            // 尝试添加 joined_at 字段
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE conversation_member ADD COLUMN joined_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP");
                log.info("成功添加 conversation_member.joined_at 字段");
            } catch (Exception e) {
                log.info("joined_at 字段可能已存在或添加失败: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("数据库修复过程中发生错误", e);
        }

        // 确保 user_follow 表存在
        ensureUserFollowTable();

        // 确保 notification 表存在
        ensureNotificationTable();

        // 确保 lost_found 表存在
        ensureLostFoundTable();

        log.info("数据库修复检查完成");
    }

    /**
     * 确保 user_follow 表存在
     */
    private void ensureUserFollowTable() {
        try {
            String createTableSql = """
                    CREATE TABLE IF NOT EXISTS `user_follow` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关注记录ID，主键自增',
                        `follower_id` BIGINT NOT NULL COMMENT '关注者用户ID',
                        `following_id` BIGINT NOT NULL COMMENT '被关注者用户ID',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_follow` (`follower_id`, `following_id`),
                        KEY `idx_following_id` (`following_id`)
                    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表'
                    """;
            jdbcTemplate.execute(createTableSql);
            log.info("user_follow 表检查/创建完成");
        } catch (Exception e) {
            log.error("创建 user_follow 表失败: {}", e.getMessage());
        }
    }

    /**
     * 确保 notification 表存在
     */
    private void ensureNotificationTable() {
        try {
            String createTableSql = """
                    CREATE TABLE IF NOT EXISTS `notification` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID，主键自增',
                        `user_id` BIGINT NOT NULL COMMENT '接收者用户ID',
                        `sender_id` BIGINT DEFAULT NULL COMMENT '发送者用户ID，系统通知为NULL',
                        `type` VARCHAR(20) NOT NULL COMMENT '通知类型: LIKE-点赞, COMMENT-评论, FOLLOW-关注, SYSTEM-系统, ADMIN-管理',
                        `title` VARCHAR(100) DEFAULT NULL COMMENT '通知标题',
                        `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
                        `target_id` BIGINT DEFAULT NULL COMMENT '关联对象ID',
                        `target_type` VARCHAR(20) DEFAULT NULL COMMENT '关联对象类型: POST-帖子, COMMENT-评论, USER-用户',
                        `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_user_id` (`user_id`),
                        KEY `idx_is_read` (`is_read`),
                        KEY `idx_type` (`type`)
                    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表'
                    """;
            jdbcTemplate.execute(createTableSql);
            log.info("notification 表检查/创建完成");
        } catch (Exception e) {
            log.error("创建 notification 表失败: {}", e.getMessage());
        }
    }

    /**
     * 确保 lost_found 表存在
     */
    private void ensureLostFoundTable() {
        try {
            String createTableSql = """
                    CREATE TABLE IF NOT EXISTS `lost_found` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '失物招领ID，主键自增',
                        `user_id` BIGINT NOT NULL COMMENT '发布者用户ID',
                        `type` VARCHAR(20) NOT NULL COMMENT '类型: LOST-丢失, FOUND-拾到',
                        `title` VARCHAR(200) NOT NULL COMMENT '标题',
                        `description` TEXT DEFAULT NULL COMMENT '详细描述',
                        `images` JSON DEFAULT NULL COMMENT '图片URL数组，JSON格式存储',
                        `location` VARCHAR(200) DEFAULT NULL COMMENT '丢失/拾到地点',
                        `contact_info` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
                        `status` VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '状态: OPEN-进行中, CLOSED-已解决',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
                        PRIMARY KEY (`id`),
                        KEY `idx_user_id` (`user_id`),
                        KEY `idx_type` (`type`),
                        KEY `idx_status` (`status`),
                        KEY `idx_created_at` (`created_at`)
                    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领表'
                    """;
            jdbcTemplate.execute(createTableSql);
            log.info("lost_found 表检查/创建完成");

            // 尝试添加 priority 字段
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE lost_found ADD COLUMN priority VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级: NORMAL-普通, PINNED-置顶'");
                log.info("成功添加 lost_found.priority 字段");
            } catch (Exception e) {
                log.info("lost_found.priority 字段可能已存在: {}", e.getMessage());
            }

            // 尝试添加 contact_info 字段 (修复 Unknown column 'contact_info' 错误)
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE lost_found ADD COLUMN contact_info VARCHAR(100) DEFAULT NULL COMMENT '联系方式'");
                log.info("成功添加 lost_found.contact_info 字段");
            } catch (Exception e) {
                log.info("lost_found.contact_info 字段可能已存在: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("创建 lost_found 表失败: {}", e.getMessage());
        }

        // 确保默认用户存在
        ensureDefaultUser();
    }

    /**
     * 确保至少有一个默认用户存在 (避免外键约束错误)
     */
    private void ensureDefaultUser() {
        try {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE username = 'admin'", Long.class);

            if (count == null || count == 0) {
                String adminHash = passwordEncoder.encode("admin123");
                String insertUserSql = """
                        INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
                        VALUES ('admin', ?, '管理员', 'ADMIN', 'NORMAL')
                        """;
                jdbcTemplate.update(insertUserSql, adminHash);
                log.info("已创建默认管理员用户 (admin/admin123)");
            }
        } catch (Exception e) {
            log.error("检查/创建默认用户失败: {}", e.getMessage());
        }
    }
}
