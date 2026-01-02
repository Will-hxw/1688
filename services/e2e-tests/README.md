# E2E 端到端集成测试

## 概述

本模块包含微服务架构的端到端集成测试，验证完整业务流程和异常场景。

## 测试内容

### 1. 完整业务流程测试 (FullBusinessFlowE2ETest)

测试流程：注册 → 登录 → 发布商品 → 下单 → 发货 → 收货 → 评价

验证跨服务调用：
- Auth-User Service: 用户注册、登录
- Product Service: 商品发布、库存管理
- Order Service: 订单创建、状态流转
- Review Service: 评价提交

### 2. 异常场景测试 (ExceptionScenariosE2ETest)

- 场景1: 库存不足下单失败 (Requirements 3.7)
- 场景2: 重复下单幂等返回 (Requirements 4.1)
- 场景3: 非法状态转换拒绝 (Requirements 4.8)
- 场景4: 取消订单库存回滚

## 运行前提

1. 启动所有微服务：
   ```bash
   docker-compose up -d
   ```

2. 等待所有服务健康检查通过：
   ```bash
   docker-compose ps
   ```
   确保所有服务状态为 `healthy`

## 运行测试

```bash
# 在项目根目录执行
cd services
mvn test -pl e2e-tests -DskipE2ETests=false

# 或指定 Gateway 地址
mvn test -pl e2e-tests -DskipE2ETests=false -DGATEWAY_URL=http://localhost:8080
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| GATEWAY_URL | http://localhost:8080 | Gateway 服务地址 |

## 注意事项

1. E2E 测试默认跳过（`skipE2ETests=true`），需要手动启用
2. 测试会创建真实数据，建议在测试环境运行
3. 每次测试使用唯一用户名和幂等键，避免数据冲突
