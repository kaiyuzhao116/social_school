# CampusConnect API - 校园脉动后端服务

## 技术栈
- Java 17
- Spring Boot 3.2.0
- MyBatis Plus 3.5.5
- MySQL 8.0
- JWT 认证

## 项目结构
```
src/main/java/com/campusconnect/
├── CampusConnectApplication.java  # 启动类
├── common/                         # 通用类
│   ├── Result.java                # 统一响应格式
│   └── PageResult.java            # 分页结果
├── config/                         # 配置类
│   ├── SecurityConfig.java        # 安全配置
│   └── MybatisPlusConfig.java     # MyBatis Plus配置
├── controller/                     # 控制器
│   ├── AuthController.java        # 认证接口
│   ├── AdminController.java       # 管理后台接口
│   ├── PostController.java        # 帖子接口
│   └── UserController.java        # 用户接口
├── entity/                         # 实体类
├── mapper/                         # 数据访问层
├── security/                       # 安全相关
│   ├── JwtUtil.java               # JWT工具类
│   ├── JwtAuthenticationFilter.java
│   └── UserPrincipal.java
└── service/                        # 业务逻辑层
```

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 2. 创建数据库
```sql
-- 执行 src/main/resources/db/init.sql
```

### 3. 修改配置
编辑 `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campusconnect
    username: root
    password: 你的密码
```

### 4. 运行项目
```bash
# 方式1: Maven
mvn spring-boot:run

# 方式2: IDE
运行 CampusConnectApplication.java
```

### 5. 测试API
```bash
# 登录获取Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 测试账号
# 管理员: admin / admin123
# 普通用户: user1 / admin123
```

## API接口

### 认证接口 `/auth`
- POST `/login` - 登录
- POST `/register` - 注册
- POST `/refresh` - 刷新Token

### 管理后台 `/admin` (需要ADMIN/MODERATOR角色)
- GET `/dashboard/stats` - 仪表盘统计
- GET/PUT `/users` - 用户管理
- GET/POST/DELETE `/posts` - 帖子管理
- POST `/posts/{id}/moderate` - 审核帖子
- GET/POST `/verifications` - 身份认证
- GET/POST `/reports` - 举报管理
- GET/POST/PUT/DELETE `/announcements` - 公告管理
- GET/POST/PUT/DELETE `/activities` - 活动管理

### 用户接口 `/user`
- GET `/me` - 获取当前用户
- PUT `/me` - 更新个人信息

### 帖子接口 `/posts`
- GET `/` - 获取帖子列表
- GET `/hot` - 热门帖子
- POST `/` - 发布帖子
- POST `/{id}/like` - 点赞

## 前端对接

在前端 `dataService.js` 中设置:
```javascript
const USE_REAL_API = true  // 启用后端API
```

确保前端 `.env.development` 配置:
```
VITE_API_BASE_URL=http://localhost:8080/api
```
