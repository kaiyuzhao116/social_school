-- =====================================================
-- CampusConnect 校园脉动 - 数据库建表SQL
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `campusconnect` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `campusconnect`;

-- =====================================================
-- 1. 用户表
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键自增',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名，用于登录，3-20位，仅字母数字下划线点和破折号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码，BCrypt加密存储',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称，显示名称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '个人主页封面图URL',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱，唯一',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别: MALE-男, FEMALE-女, OTHER-其他',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER-普通用户, MODERATOR-协管员, ADMIN-管理员',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL-正常, BANNED-封禁',
    `verify_status` VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '认证状态: NONE-未认证, PENDING-审核中, VERIFIED-已认证',
    `verify_type` VARCHAR(20) DEFAULT NULL COMMENT '认证类型: STUDENT-学生, TEACHER-教师, ORG-组织/部门',
    `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
    `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
    `post_count` INT NOT NULL DEFAULT 0 COMMENT '发帖数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`),
    KEY `idx_verify_status` (`verify_status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 帖子表
-- =====================================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID，主键自增',
    `user_id` BIGINT NOT NULL COMMENT '发布者用户ID',
    `content` TEXT NOT NULL COMMENT '帖子内容',
    `images` JSON DEFAULT NULL COMMENT '图片URL数组，JSON格式存储',
    `tags` JSON DEFAULT NULL COMMENT '标签数组，JSON格式存储',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PUBLISHED-已发布, PENDING-审核中, REJECTED-已拒绝, FLAGGED-被标记',
    `is_pinned` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否, 1-是',
    `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名: 0-否, 1-是',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享数',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
    `ai_safe` TINYINT DEFAULT NULL COMMENT 'AI审核结果: 0-不安全, 1-安全',
    `ai_reason` VARCHAR(255) DEFAULT NULL COMMENT 'AI审核原因说明',
    `ai_confidence` INT DEFAULT NULL COMMENT 'AI审核置信度(0-100)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_pinned` (`is_pinned`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- =====================================================
-- 3. 评论表
-- =====================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键自增',
    `post_id` BIGINT NOT NULL COMMENT '所属帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者用户ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID，用于回复功能，NULL表示一级评论',
    `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复的目标用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =====================================================
-- 4. 身份认证申请表
-- =====================================================
DROP TABLE IF EXISTS `verification`;
CREATE TABLE `verification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '认证申请ID，主键自增',
    `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
    `identity_type` VARCHAR(20) NOT NULL COMMENT '认证类型: STUDENT-学生, TEACHER-教师, ORG-组织/部门',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名/组织名称',
    `id_number` VARCHAR(50) DEFAULT NULL COMMENT '学号/工号/组织编号',
    `department` VARCHAR(100) DEFAULT NULL COMMENT '院系/部门',
    `id_card_image` VARCHAR(500) DEFAULT NULL COMMENT '证件照片URL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_identity_type` (`identity_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身份认证申请表';

-- =====================================================
-- 5. 举报表
-- =====================================================
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID，主键自增',
    `reporter_id` BIGINT NOT NULL COMMENT '举报人用户ID',
    `target_id` BIGINT NOT NULL COMMENT '被举报对象ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '举报对象类型: POST-帖子, COMMENT-评论, USER-用户',
    `reason` VARCHAR(100) NOT NULL COMMENT '举报原因',
    `description` TEXT DEFAULT NULL COMMENT '详细描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态: PENDING-待处理, RESOLVED-已解决, IGNORED-已忽略',
    `action` VARCHAR(20) DEFAULT NULL COMMENT '处理动作: DELETE-删除内容, BAN-封禁用户, WARN-警告',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- =====================================================
-- 6. 公告表
-- =====================================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID，主键自增',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `publisher` VARCHAR(100) DEFAULT NULL COMMENT '发布部门名称',
    `publisher_id` BIGINT DEFAULT NULL COMMENT '发布人用户ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PUBLISHED-已发布',
    `priority` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级: NORMAL-普通, PINNED-置顶',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '阅读次数',
    `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_published_at` (`published_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- =====================================================
-- 7. 活动表
-- =====================================================
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID，主键自增',
    `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
    `description` TEXT DEFAULT NULL COMMENT '活动描述',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    `location` VARCHAR(200) DEFAULT NULL COMMENT '活动地点',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `organizer` VARCHAR(100) DEFAULT NULL COMMENT '主办方名称',
    `organizer_id` BIGINT DEFAULT NULL COMMENT '发布人用户ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'REGISTERING' COMMENT '状态: REGISTERING-报名中, ONGOING-进行中, ENDED-已结束',
    `max_participants` INT DEFAULT NULL COMMENT '最大参与人数，NULL表示不限',
    `participant_count` INT NOT NULL DEFAULT 0 COMMENT '当前参与人数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- =====================================================
-- 8. 活动报名表
-- =====================================================
DROP TABLE IF EXISTS `activity_registration`;
CREATE TABLE `activity_registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名记录ID，主键自增',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `user_id` BIGINT NOT NULL COMMENT '报名用户ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'REGISTERED' COMMENT '状态: REGISTERED-已报名, CANCELLED-已取消, ATTENDED-已参加',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- =====================================================
-- 8.5 失物招领表
-- =====================================================
DROP TABLE IF EXISTS `lost_found`;
CREATE TABLE `lost_found` (
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领表';

-- =====================================================
-- 9. 通知表
-- =====================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- =====================================================
-- 10. 用户关注关系表
-- =====================================================
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关注记录ID，主键自增',
    `follower_id` BIGINT NOT NULL COMMENT '关注者用户ID',
    `following_id` BIGINT NOT NULL COMMENT '被关注者用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`follower_id`, `following_id`),
    KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

-- =====================================================
-- 11. 帖子点赞表
-- =====================================================
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID，主键自增',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- =====================================================
-- 12. 帖子收藏表
-- =====================================================
DROP TABLE IF EXISTS `post_collect`;
CREATE TABLE `post_collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏记录ID，主键自增',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '收藏用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏表';

-- =====================================================
-- 13. 会话表（私聊/群聊）
-- =====================================================
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID，主键自增',
    `name` VARCHAR(100) DEFAULT NULL COMMENT '群聊名称，私聊为空',
    `type` VARCHAR(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '会话类型: PRIVATE-私聊, GROUP-群聊',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '群聊头像URL',
    `owner_id` BIGINT DEFAULT NULL COMMENT '群主用户ID',
    `announcement` TEXT DEFAULT NULL COMMENT '群公告',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- =====================================================
-- 14. 会话成员表
-- =====================================================
DROP TABLE IF EXISTS `conversation_member`;
CREATE TABLE `conversation_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员记录ID，主键自增',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '成员角色: OWNER-群主, ADMIN-管理员, MEMBER-成员',
    `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `last_read_at` DATETIME DEFAULT NULL COMMENT '最后阅读时间',
    `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conv_user` (`conversation_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话成员表';

-- =====================================================
-- 15. 消息表
-- =====================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID，主键自增',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT-文本, IMAGE-图片, FILE-文件',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =====================================================
-- 初始数据
-- =====================================================

-- 插入管理员账号 (密码: admin123，使用BCrypt加密)
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `role`, `status`, `verify_status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'https://picsum.photos/100/100?random=100', 'ADMIN', 'NORMAL', 'VERIFIED');

-- 插入测试用户 (密码都是: admin123)
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `role`, `status`) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '林雨晴', 'https://picsum.photos/100/100?random=1', 'USER', 'NORMAL'),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张伟', 'https://picsum.photos/100/100?random=2', 'MODERATOR', 'NORMAL'),
('user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王志强', 'https://picsum.photos/100/100?random=3', 'USER', 'BANNED'),
('user4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈思思', 'https://picsum.photos/100/100?random=4', 'USER', 'NORMAL');

-- 插入测试帖子
INSERT INTO `post` (`user_id`, `content`, `status`, `is_pinned`, `like_count`, `comment_count`) VALUES
(2, '求助：有人捡到我的设计概论笔记吗？可能是落在第三教学楼201教室了。', 'PUBLISHED', 1, 15, 3),
(2, '二食堂的阿姨手抖得太厉害了，这饭根本没法吃，建议大家都别去！垃圾食堂！', 'PENDING', 0, 0, 0),
(4, '兼职代课，需要的私聊，价格公道，包过。', 'PENDING', 0, 0, 0),
(5, '今天在图书馆自习，效率超高！推荐大家来三楼的安静区域。', 'PUBLISHED', 0, 28, 5);

-- 插入测试身份认证申请
INSERT INTO `verification` (`user_id`, `identity_type`, `real_name`, `id_number`, `department`, `id_card_image`, `status`) VALUES
(2, 'STUDENT', '林雨晴', '20240012', '计算机科学与技术学院', 'https://picsum.photos/400/250?random=4', 'PENDING'),
(3, 'TEACHER', '李浩然', 'T20240088', '法学院', 'https://picsum.photos/400/250?random=5', 'PENDING'),
(5, 'ORG', '校吉他社', 'ORG-003', '校团委社团部', 'https://picsum.photos/400/250?random=6', 'PENDING');

-- 插入测试举报
INSERT INTO `report` (`reporter_id`, `target_id`, `target_type`, `reason`, `status`) VALUES
(3, 2, 'POST', '言语辱骂 / 恶意攻击', 'PENDING'),
(1, 3, 'POST', '疑似违规广告', 'PENDING');

-- 插入测试公告
INSERT INTO `announcement` (`title`, `content`, `publisher`, `publisher_id`, `status`, `priority`, `view_count`, `published_at`) VALUES
('关于2024年春季学期校园文化节的通知', '各位同学，2024年春季校园文化节将于下周三正式启动。届时将有社团展示、创意集市等丰富多彩的活动，欢迎大家踊跃参加。详情请查看附件文档。', '校团委', 1, 'PUBLISHED', 'PINNED', 1254, NOW()),
('图书馆系统维护公告', '为了提供更好的服务，图书馆管理系统将于本周六凌晨 00:00 至 04:00 进行升级维护，期间将暂停借阅服务。', '图书馆', 1, 'DRAFT', 'NORMAL', 0, NULL),
('严禁在宿舍使用大功率电器的提醒', '近期发现部分寝室违规使用大功率电器，存在严重安全隐患。宿管科将进行突击检查，违者将按校规处理。', '后勤部', 1, 'PUBLISHED', 'NORMAL', 890, NOW());

-- 插入测试活动
INSERT INTO `activity` (`title`, `description`, `cover_image`, `location`, `start_time`, `organizer`, `organizer_id`, `status`, `max_participants`, `participant_count`) VALUES
('校园十佳歌手大赛海选', '展示你的歌喉，成为校园之星！', 'https://picsum.photos/400/300?random=50', '大学生活动中心 301', '2024-11-10 18:00:00', '校学生会文艺部', 1, 'REGISTERING', 200, 156),
('人工智能前沿技术讲座', '邀请业界专家分享AI最新发展趋势', 'https://picsum.photos/400/300?random=51', '图书馆报告厅', '2024-11-05 14:30:00', '计算机学院', 1, 'ONGOING', 300, 300);

-- =====================================================
-- 完成提示
-- =====================================================
SELECT '数据库初始化完成！' AS message;
SELECT '测试账号: admin/admin123 (管理员), user1/admin123 (普通用户)' AS accounts;
