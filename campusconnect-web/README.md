# CampusConnect 校园连接

一个基于 Vue 3 的校园社交平台应用。

## 技术栈

- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Pinia** - 状态管理
- **Vue Router** - 路由管理
- **Axios** - HTTP 请求
- **Lucide Vue Next** - 图标库

## 项目结构

```
src/
├── api/          # API 接口
├── components/   # 公共组件
├── router/       # 路由配置
├── services/     # 服务层
├── stores/       # Pinia 状态管理
├── types/        # 类型定义
├── utils/        # 工具函数
├── views/        # 页面视图
├── App.vue       # 根组件
└── main.js       # 入口文件
```

## 快速开始

### 环境要求

- Node.js >= 16

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览构建结果

```bash
npm run preview
```

## 环境变量

开发环境配置文件为 `.env.development`，主要配置项：

```
# API 配置
VITE_API_BASE_URL=http://localhost:8080/api

# AI 服务配置 (SiliconFlow)
VITE_AI_API_URL=https://api.siliconflow.cn/v1/chat/completions
VITE_AI_MODEL=THUDM/GLM-4.1V-9B-Thinking
VITE_SILICON_API_KEY=你的SiliconFlow API密钥
```
