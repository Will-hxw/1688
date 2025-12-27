# CQU抽象集市 - 校园二手商品交易平台

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

### 环境要求

- Docker 20.10+
- Docker Compose 2.0+

### 一键部署

1. **启动服务**
```bash
docker-compose up -d --build backend nginx
```
2. **访问应用**
- 前端页面: http://localhost:8082
- API文档：http://localhost:8081/api/swagger-ui.html
- 默认管理员账号: xiaoweihua / cqu
3. 数据库
```bash
docker exec -it cqu-marketplace-mysql mysql -u root -proot cqu_marketplace
```
4. demo
```bash
docker cp demo/demo-data.sql cqu-marketplace-mysql:/tmp/demo-data.sql

docker exec cqu-marketplace-mysql mysql -u root -proot --default-character-set=utf8mb4 cqu_marketplace -e "source /tmp/demo-data.sql"
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
| NGINX_PORT | Nginx对外端口 | 8080 |
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
