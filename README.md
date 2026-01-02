# CQU抽象集市 - 校园二手商品交易平台

## 项目简介

校园二手商品交易平台，支持用户发布、搜索、购买二手商品，包含完整的订单流程和评价系统。

## 架构模式

本项目采用**微服务架构**，将单体应用拆分为 5 个独立服务 + 1 个 API 网关：

```
                    ┌─────────────┐
                    │   Nginx     │
                    │  (前端+代理) │
                    └──────┬──────┘
                           │ /api/*
                    ┌──────▼──────┐
                    │   Gateway   │
                    │  Service    │
                    │ (JWT验证)   │
                    └──────┬──────┘
           ┌───────────────┼───────────────┐
           │               │               │
    ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
    │ Auth-User   │ │  Product    │ │   Order     │
    │  Service    │ │  Service    │ │  Service    │
    └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
           │               │               │
    ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
    │ auth-user-db│ │ product-db  │ │  order-db   │
    └─────────────┘ └─────────────┘ └─────────────┘
                                           │
                                    ┌──────▼──────┐
                                    │   Review    │
                                    │  Service    │
                                    └──────┬──────┘
                                           │
                                    ┌──────▼──────┐
                                    │ review-db   │
                                    └─────────────┘
```

## 技术栈

### 后端微服务
- Java 17
- Spring Boot 3.2.x
- Spring Cloud Gateway（API 网关）
- OpenFeign（服务间通信）
- MyBatis-Plus 3.5.x
- MySQL 8.0（每服务独立数据库）
- Redis 7.x（幂等键、缓存）
- JWT 认证
- Flyway 数据库迁移
- Spring Boot Actuator（健康检查）

### 前端
- React 19
- TypeScript
- Vite
- Ant Design 5
- Zustand（状态管理）

### 部署
- Docker & Docker Compose
- Nginx 反向代理

## 项目结构

```
cqu-marketplace/
├── backend/                    # 原单体应用（保留作为参考）
├── services/                   # 微服务目录
│   ├── pom.xml                 # 父 POM（依赖版本管理）
│   ├── common/                 # 共享组件模块
│   │   └── src/main/java/      # Result、PageResult、BusinessException、枚举类
│   ├── gateway-service/        # API 网关（端口 8080）
│   │   └── src/main/java/      # 路由配置、JWT 验证、CORS 处理
│   ├── auth-user-service/      # 认证用户服务（端口 8081）
│   │   └── src/main/java/      # 注册、登录、用户管理
│   ├── product-service/        # 商品服务（端口 8082）
│   │   └── src/main/java/      # 商品 CRUD、库存管理、文件上传
│   ├── order-service/          # 订单服务（端口 8083）
│   │   └── src/main/java/      # 订单创建（幂等）、状态机、商品快照
│   └── review-service/         # 评价服务（端口 8084）
│       └── src/main/java/      # 评价管理
├── frontend/                   # 前端 React 项目
│   ├── src/
│   │   ├── api/                # API 请求封装
│   │   ├── components/         # 公共组件
│   │   ├── pages/              # 页面组件
│   │   ├── stores/             # Zustand 状态管理
│   │   └── utils/              # 工具函数
│   └── package.json
├── nginx/
│   ├── Dockerfile              # Nginx 镜像构建文件（含前端构建）
│   └── nginx.conf              # Nginx 配置文件
├── uploads/                    # 上传文件存储目录
├── docker-compose.yml          # Docker Compose 编排文件
├── .env                        # 环境变量配置
└── README.md
```


## 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- JDK 17+（本地开发）
- Maven 3.8+（本地开发）
- Node.js 18+（前端开发）

## 快速部署

### 一键启动（推荐）

```bash
# 启动所有微服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f gateway
```

### 访问应用

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:8082 | React SPA |
| API 网关 | http://localhost:8080 | 所有 API 入口 |
| Auth-User 服务 | http://localhost:8081 | 认证用户服务 |
| Product 服务 | http://localhost:8082 | 商品服务 |
| Order 服务 | http://localhost:8083 | 订单服务 |
| Review 服务 | http://localhost:8084 | 评价服务 |

### 默认账号

- 管理员：`xiaoweihua` / `cqu`
- 演示用户：`zhangsan` / `123456`

## 环境变量配置

### 数据库配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| AUTH_USER_DB_HOST | 用户数据库主机 | auth-user-db |
| AUTH_USER_DB_PORT | 用户数据库端口 | 3306 |
| AUTH_USER_DB_NAME | 用户数据库名 | auth_user_db |
| PRODUCT_DB_HOST | 商品数据库主机 | product-db |
| PRODUCT_DB_PORT | 商品数据库端口 | 3306 |
| PRODUCT_DB_NAME | 商品数据库名 | product_db |
| ORDER_DB_HOST | 订单数据库主机 | order-db |
| ORDER_DB_PORT | 订单数据库端口 | 3306 |
| ORDER_DB_NAME | 订单数据库名 | order_db |
| REVIEW_DB_HOST | 评价数据库主机 | review-db |
| REVIEW_DB_PORT | 评价数据库端口 | 3306 |
| REVIEW_DB_NAME | 评价数据库名 | review_db |

### 服务间通信配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| AUTH_USER_SERVICE_URL | 用户服务地址 | http://auth-user-service:8081 |
| PRODUCT_SERVICE_URL | 商品服务地址 | http://product-service:8082 |
| ORDER_SERVICE_URL | 订单服务地址 | http://order-service:8083 |
| REVIEW_SERVICE_URL | 评价服务地址 | http://review-service:8084 |

### 其他配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| REDIS_HOST | Redis 主机 | redis |
| REDIS_PORT | Redis 端口 | 6379 |
| JWT_SECRET | JWT 签名密钥 | (默认密钥) |
| NGINX_PORT | Nginx 对外端口 | 8082 |

## Nginx 路由规则

| 路径 | 目标 | 说明 |
|------|------|------|
| `/api/**` | Gateway Service | API 请求转发到网关 |
| `/uploads/**` | Product Service | 静态文件（上传的图片） |
| `/*` | 前端静态资源 | React SPA |

## 主要 API 端点

### 认证模块 → Auth-User Service

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| GET | /api/users/me | 当前用户信息 |

### 商品模块 → Product Service

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | /api/products | 商品搜索 |
| POST | /api/products | 发布商品 |
| POST | /api/upload/image | 上传图片 |

### 订单模块 → Order Service

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/orders | 创建订单（幂等） |
| GET | /api/orders/buyer | 买家订单列表 |
| GET | /api/orders/seller | 卖家订单列表 |
| PUT | /api/orders/{id}/ship | 发货 |
| PUT | /api/orders/{id}/receive | 确认收货 |
| PUT | /api/orders/{id}/cancel | 取消订单 |

### 评价模块 → Review Service

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/reviews | 提交评价 |
| GET | /api/reviews/product/{id} | 商品评价列表 |

### 管理后台

| 方法 | 端点 | 目标服务 | 说明 |
|------|------|----------|------|
| GET | /api/admin/users | Auth-User | 用户管理 |
| GET | /api/admin/products | Product | 商品管理 |
| GET | /api/admin/orders | Order | 订单管理 |
| GET | /api/admin/reviews | Review | 评价管理 |

## 服务健康检查

所有微服务暴露 `/actuator/health` 端点：

```bash
# 检查网关健康状态
curl http://localhost:8080/actuator/health

# 检查各服务健康状态
curl http://localhost:8081/actuator/health  # Auth-User
curl http://localhost:8082/actuator/health  # Product
curl http://localhost:8083/actuator/health  # Order
curl http://localhost:8084/actuator/health  # Review
```


## 遇到的问题和解决方案

### 问题 1：商品评价路由 404

**现象**：前端调用 `/api/products/{id}/reviews` 获取商品评价时返回 404。

**原因**：
- 前端调用路径：`/api/products/{id}/reviews`
- 后端实际接口：`/api/reviews/product/{id}`
- Gateway 将 `/api/products/**` 路由到 Product Service，但 Product Service 没有该端点

**解决方案**：在 Gateway 路由配置中添加特殊路由，将 `/api/products/{id}/reviews` 重写并转发到 Review Service：

```yaml
# services/gateway-service/src/main/resources/application.yml
routes:
  # 必须放在 product-service 路由之前
  - id: product-reviews-service
    uri: ${services.review.url:http://review-service:8084}
    predicates:
      - Path=/api/products/{productId}/reviews
    filters:
      - StripPrefix=1
      - RewritePath=/products/(?<productId>\d+)/reviews, /reviews/product/$\{productId}
```

---

### 问题 2：评价管理页面看不到商品名称和评论者昵称

**现象**：管理后台的评价管理页面，商品名称和评论者昵称显示为空。

**原因**：微服务架构下，Review Service 只存储 `productId` 和 `buyerId`，不存储冗余的商品名称和用户昵称。原来的 `convertToVO()` 方法没有跨服务查询这些信息。

**解决方案**：

1. 在 Review Service 中添加 `ProductClient` 和 `UserClient` Feign 客户端：

```java
// services/review-service/src/main/java/.../client/ProductClient.java
@FeignClient(name = "product-service", url = "${services.product.url}")
public interface ProductClient {
    @GetMapping("/internal/products/{id}")
    Result<ProductInfo> getProductInfo(@PathVariable("id") Long productId);
}

// services/review-service/src/main/java/.../client/UserClient.java
@FeignClient(name = "auth-user-service", url = "${services.auth-user.url}")
public interface UserClient {
    @GetMapping("/internal/users/{id}")
    Result<UserInfo> getUserInfo(@PathVariable("id") Long userId);
}
```

2. 在 `application.yml` 中添加服务 URL 配置：

```yaml
services:
  product:
    url: ${PRODUCT_SERVICE_URL:http://localhost:8082}
  auth-user:
    url: ${AUTH_USER_SERVICE_URL:http://localhost:8081}
```

3. 修改 `ReviewServiceImpl.listAllReviews()` 方法，批量查询并填充商品名称和买家昵称。

4. 在 `docker-compose.yml` 中为 review-service 添加环境变量：

```yaml
review-service:
  environment:
    - PRODUCT_SERVICE_URL=http://product-service:8082
    - AUTH_USER_SERVICE_URL=http://auth-user-service:8081
```

---

### 问题 3：POST /api/products 创建商品返回 401 未认证

**现象**：已登录用户发布商品时返回 401 错误。

**原因**：Gateway 的 JWT 过滤器将 `/api/products` 路径配置为公开路径（无需认证），导致 POST 请求也跳过了 JWT 验证，没有添加 `X-User-Id` 请求头。

**解决方案**：修改 `JwtAuthFilter.java` 的公开路径判断逻辑，只允许 GET 请求访问商品相关的公开接口：

```java
// services/gateway-service/src/main/java/.../filter/JwtAuthFilter.java
private boolean isPublicPath(String path, String method) {
    // 精确匹配（仅登录注册）
    if (PUBLIC_PATHS.contains(path)) {
        return true;
    }
    
    // GET 请求的公开路径
    if ("GET".equalsIgnoreCase(method)) {
        // 前缀匹配
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        // GET /api/products 商品列表公开
        if (path.equals("/api/products") || path.startsWith("/api/products?")) {
            return true;
        }
        // GET /api/products/{id} 商品详情公开
        if (path.matches("/api/products/\\d+")) {
            return true;
        }
    }
    
    return false;
}
```

---

### 问题 4：Flyway 迁移校验和不匹配

**现象**：修改 Flyway 迁移脚本后启动服务报错 `Migration checksum mismatch`。

**原因**：Flyway 会记录已执行迁移脚本的校验和，修改已执行的脚本会导致校验失败。

**解决方案**：删除数据库卷重新初始化：

```bash
docker-compose down -v
docker-compose up -d --build
```

**注意**：此操作会删除所有数据，仅适用于开发环境。生产环境应创建新的迁移脚本。
