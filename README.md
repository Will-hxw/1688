# CQU抽象集市 - 校园二手商品交易平台

## 项目简介

CQU抽象集市是一个面向校园师生的二手商品交易平台，采用前后端分离架构，UI风格参考1688电商平台。系统支持用户发布、搜索、购买二手商品，并提供完整的订单管理和评价功能。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- MyBatis-Plus 3.5.x
- MySQL 8.0
- Redis 7.x
- JWT认证
- Flyway数据库迁移

### 前端
- React 19
- TypeScript
- Vite
- Ant Design 5

### 部署
- Docker & Docker Compose
- Nginx反向代理

## 快速开始

### 环境要求

- Docker 20.10+
- Docker Compose 2.0+

### 一键部署

1. **配置环境变量**
```bash
vim .env
```

1. **创建上传目录**
```bash
mkdir -p uploads
```

1. **启动服务**
```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

5. **访问应用**
- 前端页面: http://localhost
- API文档: http://localhost/swagger-ui/index.html
- 默认管理员账号: xiaoweihua / cqu

### 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（清除所有数据）
docker-compose down -v
```

## 项目结构

```
cqu-marketplace/
├── backend/                 # 后端Spring Boot项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/       # Java源代码
│   │   │   └── resources/  # 配置文件和数据库迁移脚本
│   │   └── test/           # 测试代码
│   ├── Dockerfile          # 后端Docker镜像构建文件
│   └── pom.xml             # Maven配置
├── frontend/               # 前端React项目
│   ├── src/
│   │   ├── api/            # API请求封装
│   │   ├── components/     # 公共组件
│   │   ├── pages/          # 页面组件
│   │   ├── stores/         # Zustand状态管理
│   │   └── utils/          # 工具函数
│   └── package.json        # npm配置
├── nginx/
│   ├── Dockerfile          # Nginx镜像构建文件（含前端构建）
│   └── nginx.conf          # Nginx配置文件
├── uploads/                # 上传文件存储目录
├── docker-compose.yml      # Docker Compose编排文件
├── .env.example            # 环境变量示例
└── README.md               # 项目说明文档
```

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| NGINX_PORT | Nginx对外端口 | 80 |
| MYSQL_ROOT_PASSWORD | MySQL root密码 | root |
| MYSQL_DATABASE | 数据库名称 | cqu_marketplace |
| MYSQL_USER | 数据库用户名 | marketplace |
| MYSQL_PASSWORD | 数据库密码 | marketplace123 |
| MYSQL_PORT | MySQL对外端口 | 3306 |
| REDIS_PASSWORD | Redis密码 | (空) |
| REDIS_PORT | Redis对外端口 | 6379 |
| JWT_SECRET | JWT签名密钥 | (默认密钥) |

### Nginx路由规则

| 路径 | 目标 | 说明 |
|------|------|------|
| /api/* | 后端服务 | API接口 |
| /uploads/* | 静态文件 | 上传的图片 |
| /swagger-ui/* | 后端服务 | API文档 |
| /* | 前端静态资源 | React SPA |

## 开发指南

### 本地开发

#### 后端开发

```bash
cd backend

# 安装依赖
mvn install

# 启动开发服务器（需要本地MySQL和Redis）
mvn spring-boot:run

# 运行测试
mvn test
```

#### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### 数据库迁移

数据库迁移脚本位于 `backend/src/main/resources/db/migration/` 目录：

- `V1__init_schema.sql` - 初始化表结构
- `V2__init_admin.sql` - 初始化管理员账户

Flyway会在应用启动时自动执行迁移。

## API文档

启动服务后访问 Swagger UI 查看完整API文档：
- 本地开发: http://localhost:8080/api/swagger-ui/index.html
- Docker部署: http://localhost/swagger-ui/index.html

### 主要API端点

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | POST /api/auth/register | 用户注册 |
| 认证 | POST /api/auth/login | 用户登录 |
| 商品 | GET /api/products | 商品搜索 |
| 商品 | POST /api/products | 发布商品 |
| 订单 | POST /api/orders | 创建订单 |
| 订单 | GET /api/orders/buyer | 买家订单列表 |
| 评价 | POST /api/reviews | 提交评价 |
| 管理 | GET /api/admin/users | 用户管理 |
