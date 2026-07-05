# 校园脉动 CampusConnect

一个面向高校学生的校园综合服务平台，基于 **Spring Boot 3 + Vue 3 + MySQL + Redis + RabbitMQ + WebSocket** 构建，覆盖校园动态、活动报名、学生拼团、实时聊天室、失物招领、后台管理等业务场景。

项目已部署至腾讯云服务器，支持公网访问、后台管理、实时消息推送和运营数据看板展示。

## 在线体验

- 前台地址：http://82.157.185.69
- 后台地址：http://82.157.185.69/admin

> 测试账号可在面试或演示时提供。

---

## 项目截图

### 首页动态

![首页动态](./screenshots/frontend_home.png)

### 学生拼团

![学生拼团](./screenshots/group_buy.png)

### 实时聊天室

![实时聊天室](./screenshots/chat_room.png)

### 后台数据看板

![后台数据看板](./screenshots/admin_dashboard.png)

### 系统流控

![系统流控](./screenshots/traffic_control.png)

### 活动管理

![活动管理](./screenshots/activity_manage.png)

---

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
- Lucide Icons

### 部署

- 腾讯云轻量服务器
- Docker
- Nginx
- MySQL / Redis / RabbitMQ 容器化部署

---

## 项目亮点

### 1. 活动报名并发控制

针对校园活动限量报名场景，设计 Redis Lua + Redisson + MySQL 条件更新的并发控制方案，避免高并发下出现超卖和重复报名。

- 使用 Redis Lua 脚本原子完成库存判断、重复报名判断、库存预扣减和用户报名标记写入。
- 使用 Redisson 对同一用户同一活动维度加锁，避免重复点击、网络重试或多实例部署下的并发写入冲突。
- MySQL 层通过 `participant_count < max_participants` 条件更新防止超卖，并通过 `activity_id + user_id` 唯一索引限制重复报名。
- 设计 Redis 与 MySQL 补偿逻辑，MySQL 落库失败时回滚 Redis 库存和报名标记。
- 使用 JMeter 模拟多用户并发报名，验证限量活动场景下不会出现超卖和重复报名。

### 2. 首页热门动态缓存击穿优化

针对首页热门动态 Top10 高频访问场景，引入 Redis 缓存和互斥锁机制，降低热点 Key 失效瞬间的数据库压力。

- 热门动态优先从 Redis 读取，减少 MySQL 高频查询。
- 缓存未命中时使用 Redis 互斥锁控制回源，避免大量请求同时击穿到数据库。
- 获取锁后进行二次缓存检查，防止重复查询 MySQL。
- 查询 MySQL 后回写 Redis，并设置合理过期时间。
- 使用 Lua 脚本安全释放锁，避免误删其他线程的锁。

### 3. 学生拼团异步事件驱动

围绕学生拼团场景，使用 RabbitMQ 实现成团、取消、过期等状态事件的异步解耦。

- 拼团成功后发布事件，异步联动通知、动态、统计等模块。
- 基于 RabbitMQ TTL + DLX 实现拼团过期延迟检查，到期后自动校验数据库状态并流转为 EXPIRED。
- 使用 CompletableFuture 和自定义线程池并行聚合拼团列表、用户参与状态、发起状态和统计数据，减少首页接口等待时间。
- 通过 WebSocket 向前端推送拼团状态变化，右下角实时卡片展示拼团动态。

### 4. 实时聊天室与已读统计

实现校园聊天室模块，支持私聊、群聊、消息持久化、未读数和已读状态。

- 基于 WebSocket 实现聊天消息实时推送，维护用户与连接会话映射关系，支持同一用户多窗口在线。
- 使用 MySQL 持久化会话、成员、消息和已读记录。
- 使用 Redis Bitmap 实现消息已读人数统计，以 messageId 为 Key、userId 为 offset，通过 SETBIT 标记已读、BITCOUNT 统计已读人数。
- 支持查询消息已读用户列表和已读时间，实现“谁已读”功能。
- 实现阅后即焚消息，接收方查看后自动更新焚毁状态，再次查询时隐藏原始内容。

### 5. 后台运营数据看板

实现后台管理数据看板，展示用户数、帖子数、待审核认证、待处理举报和访问量趋势等核心指标。

- 基于 MySQL 聚合用户、帖子、认证、举报等业务数据。
- 自定义 VisitMetricsFilter 实现接口级 PV 统计，在请求入口统一埋点。
- 使用 Redis 按小时分桶记录访问量，基于 INCR 原子自增降低高频统计对 MySQL 的写入压力。
- 后台趋势接口读取最近 7 小时访问量数据，返回时间序列供前端图表渲染。
- 排除 OPTIONS、WebSocket、后台看板自身请求等无效请求，避免统计数据虚高。

---

## Redis 与 RabbitMQ 应用场景

### Redis 应用场景

项目中 Redis 主要用于缓存、并发控制、访问统计和实时状态辅助存储。

| 场景 | 设计说明 |
| --- | --- |
| 首页热门动态缓存 | 对首页 Top10 热门动态进行 Redis 缓存，减少高频访问下 MySQL 查询压力 |
| 缓存击穿优化 | 缓存失效时使用 Redis 互斥锁和双重检查机制，只允许一个线程回源数据库并重建缓存 |
| 活动报名库存控制 | 使用 Redis Lua 脚本原子完成库存判断、重复报名判断和库存预扣减 |
| 消息已读统计 | 使用 Redis Bitmap 记录消息已读状态，通过 `SETBIT` 标记已读、`BITCOUNT` 统计已读人数 |
| 后台访问量统计 | 使用 Redis 按小时分桶记录接口访问量，通过 `INCR` 原子自增生成访问趋势 |
| 系统流控配置 | 使用 Redis 存储动态流控参数，支持后台修改限流开关、QPS 阈值和降级状态 |

### RabbitMQ 应用场景

项目中 RabbitMQ 主要用于拼团事件解耦、异步通知和延迟补偿。

| 场景 | 设计说明 |
| --- | --- |
| 拼团成功事件 | 拼团成团后发布成功事件，异步联动通知、动态、统计等模块 |
| 拼团过期补偿 | 基于 RabbitMQ TTL + DLX 实现延迟检查，拼团到期后自动校验状态并流转为 EXPIRED |
| 通知异步发送 | 拼团成功、取消、过期后异步生成用户通知，降低主流程接口耗时 |
| 动态异步发布 | 拼团成功后异步生成校园动态，避免拼团主流程和帖子模块强耦合 |
| 统计异步处理 | 拼团状态变化后异步更新统计数据，提升系统扩展性 |

### 设计收益

- Redis 缓解热点数据访问压力，减少 MySQL 高频查询。
- Redis Lua 保证高并发场景下库存判断和扣减的原子性。
- RabbitMQ 解耦拼团主流程与通知、动态、统计等非核心流程。
- TTL + DLX 实现拼团过期自动补偿，避免依赖定时任务频繁扫描数据库。
- WebSocket 配合 MQ 事件，实现拼团状态变化的实时推送。

---

## 项目结构

```text
social_school
├── campusconnect-api        # Spring Boot 后端服务
├── campusconnect-web        # Vue3 前端项目
├── docs                     # 项目文档与数据库脚本
│   └── sql
│       └── campusconnect_schema.sql
├── screenshots              # 项目截图
└── README.md
```

---

# 部署说明

本项目已基于 Docker + Nginx 部署至腾讯云轻量服务器，采用前后端分离架构。

整体部署结构如下：

```text
用户浏览器
   ↓
Nginx 80 端口
   ↓
Vue dist 静态资源
   ↓
/api 请求反向代理
   ↓
Spring Boot 后端 8080
   ↓
MySQL / Redis / RabbitMQ
```

---

## 一、服务器环境

推荐服务器环境：

```text
操作系统：CentOS 7 / Ubuntu 20+
Docker：20+
MySQL：8.0
Redis：7
RabbitMQ：3-management
JDK：21
Node.js：20
Nginx：1.27
```

本项目推荐使用 Docker 部署 MySQL、Redis、RabbitMQ、后端服务和 Nginx，避免手动安装复杂环境。

---

## 二、开放端口

公网环境建议只开放：

```text
22   SSH 登录
80   Web 访问
```

以下端口不建议直接暴露到公网：

```text
3306   MySQL
6379   Redis
5672   RabbitMQ
15672  RabbitMQ 管理后台
8080   Spring Boot 后端
```

MySQL、Redis、RabbitMQ 和后端服务建议仅供服务器内部访问，由 Nginx 统一对外提供服务。

---

## 三、创建部署目录

```bash
mkdir -p /opt/campusconnect
cd /opt/campusconnect
```

拉取项目代码：

```bash
git clone https://github.com/kaiyuzhao116/social_school.git
cd social_school
```

如果 GitHub 拉取较慢，可以使用浅克隆：

```bash
git clone --depth=1 https://github.com/kaiyuzhao116/social_school.git
```

---

## 四、启动 MySQL

```bash
docker run -d \
  --name campus-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=campusconnect \
  --restart=always \
  mysql:8.0
```

查看容器状态：

```bash
docker ps
```

---

## 五、导入数据库表结构

项目数据库表结构文件位于：

```text
docs/sql/campusconnect_schema.sql
```

在项目根目录执行：

```bash
docker exec -i campus-mysql mysql -uroot -p123456 < docs/sql/campusconnect_schema.sql
```

检查表是否导入成功：

```bash
docker exec -it campus-mysql mysql -uroot -p123456 -e "use campusconnect; show tables;"
```

本仓库仅提供数据库表结构 SQL，不包含真实用户数据、密码、帖子内容等敏感信息。

---

## 六、启动 Redis

Redis 用于热门动态缓存、缓存击穿优化、活动报名库存预扣减、消息已读 Bitmap 统计、后台访问量统计和系统流控配置。

```bash
docker run -d \
  --name campus-redis \
  -p 6379:6379 \
  --restart=always \
  redis:7
```

进入 Redis：

```bash
docker exec -it campus-redis redis-cli
```

常用查看命令：

```bash
keys *
get campus:metrics:visit:hour:2026070512
```

---

## 七、启动 RabbitMQ

RabbitMQ 用于拼团成功、取消、过期等事件的异步解耦，并结合 TTL + DLX 实现拼团过期延迟补偿。

```bash
docker run -d \
  --name campus-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=123456 \
  --restart=always \
  rabbitmq:3-management
```

RabbitMQ 管理后台：

```text
http://服务器IP:15672
```

测试账号：

```text
username: admin
password: 123456
```

生产环境建议关闭 15672 公网访问，或通过安全组限制访问来源。

---

## 八、后端配置

后端配置文件：

```text
campusconnect-api/src/main/resources/application.yml
```

核心配置如下：

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/campusconnect?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456

  data:
    redis:
      host: localhost
      port: 6379
      database: 0

  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: 123456
    virtual-host: /

upload:
  path: /opt/campusconnect/uploads
  url-prefix: /uploads
```

创建上传目录：

```bash
mkdir -p /opt/campusconnect/uploads
```

由于后端容器使用 `--network host` 启动，因此后端可以通过 `localhost` 访问 MySQL、Redis 和 RabbitMQ。

---

## 九、打包后端

服务器不需要手动安装 Maven，可以直接使用 Maven Docker 镜像打包：

```bash
cd /opt/campusconnect/social_school

docker run --rm \
  -v /opt/campusconnect/social_school:/app \
  -w /app/campusconnect-api \
  maven:3.9.9-eclipse-temurin-21 \
  mvn clean package -DskipTests
```

打包完成后查看 jar：

```bash
ls -lh campusconnect-api/target
```

正常会生成：

```text
campusconnect-api-1.0.0.jar
```

---

## 十、启动后端服务

```bash
docker rm -f campus-api 2>/dev/null

docker run -d \
  --name campus-api \
  --network host \
  -e TZ=Asia/Shanghai \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/campusconnect/uploads:/opt/campusconnect/uploads \
  -v /opt/campusconnect/social_school/campusconnect-api/target/campusconnect-api-1.0.0.jar:/app/app.jar \
  --restart=always \
  eclipse-temurin:21-jre \
  java -Duser.timezone=Asia/Shanghai -jar /app/app.jar
```

查看后端日志：

```bash
docker logs -f campus-api
```

看到以下内容说明后端启动成功：

```text
Tomcat started on port 8080 with context path '/api'
Started CampusConnectApplication
```

测试后端接口：

```bash
curl http://127.0.0.1:8080/api/admin/dashboard/stats
```

如果返回 JSON，说明后端服务正常。

---

## 十一、打包前端

```bash
cd /opt/campusconnect/social_school/campusconnect-web

docker run --rm \
  -v /opt/campusconnect/social_school/campusconnect-web:/app \
  -w /app \
  node:20-alpine \
  sh -c "npm config set registry https://registry.npmmirror.com && npm install && npm run build"
```

打包完成后会生成：

```text
campusconnect-web/dist
```

检查 dist：

```bash
ls -lh dist
```

正常应包含：

```text
index.html
assets
```

---

## 十二、配置 Nginx

创建 Nginx 配置目录：

```bash
mkdir -p /opt/campusconnect/nginx
```

写入 Nginx 配置：

```bash
cat > /opt/campusconnect/nginx/default.conf <<'EOF'
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    # Vue 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        proxy_connect_timeout 300s;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    # 上传文件访问
    location /uploads/ {
        proxy_pass http://127.0.0.1:8080/api/uploads/;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket 代理
    location /ws/ {
        proxy_pass http://127.0.0.1:8080/api/ws/;

        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api/ws/ {
        proxy_pass http://127.0.0.1:8080/api/ws/;

        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF
```

---

## 十三、启动 Nginx

```bash
docker rm -f campus-nginx 2>/dev/null

docker run -d \
  --name campus-nginx \
  --network host \
  -v /opt/campusconnect/social_school/campusconnect-web/dist:/usr/share/nginx/html:ro \
  -v /opt/campusconnect/nginx/default.conf:/etc/nginx/conf.d/default.conf:ro \
  --restart=always \
  nginx:1.27-alpine
```

查看容器状态：

```bash
docker ps
```

正常应包含：

```text
campus-nginx
campus-api
campus-mysql
campus-redis
campus-rabbitmq
```

测试前端：

```bash
curl http://127.0.0.1
```

测试 Nginx 反向代理后端：

```bash
curl http://127.0.0.1/api/admin/dashboard/stats
```

---

## 十四、访问项目

前台访问地址：

```text
http://服务器IP
```

后台管理地址：

```text
http://服务器IP/admin
```

后端接口地址：

```text
http://服务器IP/api
```

---

## 十五、常用运维命令

查看运行中的容器：

```bash
docker ps
```

查看后端日志：

```bash
docker logs -f campus-api
```

查看 Nginx 日志：

```bash
docker logs -f campus-nginx
```

重启后端：

```bash
docker restart campus-api
```

重启前端代理：

```bash
docker restart campus-nginx
```

重启 Redis：

```bash
docker restart campus-redis
```

重启 RabbitMQ：

```bash
docker restart campus-rabbitmq
```

---

## 十六、代码更新与重新部署

本地修改代码后提交：

```bash
git add .
git commit -m "更新功能"
git push origin main
```

服务器拉取最新代码：

```bash
cd /opt/campusconnect/social_school
git pull
```

如果修改了后端代码，重新打包并重启后端：

```bash
docker run --rm \
  -v /opt/campusconnect/social_school:/app \
  -w /app/campusconnect-api \
  maven:3.9.9-eclipse-temurin-21 \
  mvn clean package -DskipTests

docker restart campus-api
```

如果修改了前端代码，重新打包并重启 Nginx：

```bash
cd /opt/campusconnect/social_school/campusconnect-web

docker run --rm \
  -v /opt/campusconnect/social_school/campusconnect-web:/app \
  -w /app \
  node:20-alpine \
  sh -c "npm config set registry https://registry.npmmirror.com && npm install && npm run build"

docker restart campus-nginx
```

---

## 十七、部署顺序总结

推荐按照以下顺序部署：

```text
1. 安装 Docker
2. 拉取项目代码
3. 启动 MySQL
4. 导入 docs/sql/campusconnect_schema.sql
5. 启动 Redis
6. 启动 RabbitMQ
7. 修改后端 application.yml
8. 打包并启动 Spring Boot 后端
9. 打包 Vue 前端
10. 启动 Nginx
11. 访问 http://服务器IP
```