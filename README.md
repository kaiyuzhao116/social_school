# 校园脉动 CampusConnect

一个面向高校学生的校园综合服务平台，基于 Spring Boot 3 + Vue 3 + MySQL + Redis + RabbitMQ + WebSocket 构建，覆盖校园动态、活动报名、学生拼团、实时聊天室、失物招领、后台管理等业务场景。

项目已部署至腾讯云服务器，支持公网访问、后台管理、实时消息推送和运营数据看板展示。

## 在线体验

- 前台地址：http://82.157.185.69
- 后台地址：http://82.157.185.69/admin

> 测试账号可在面试或演示时提供。

## 技术栈

### 后端

- Spring Boot 3.5
- Spring Security + JWT
- MyBatis-Plus
- MySQL 8
- Redis / Redisson
- RabbitMQ
- WebSocket
- CompletableFuture
- Docker

### 前端

- Vue 3
- Vite
- Pinia
- Vue Router
- Tailwind CSS
- Axios

### 部署

- 腾讯云轻量服务器
- Docker
- Nginx
- MySQL / Redis / RabbitMQ 容器化部署