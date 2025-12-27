# 场景C测试数据检查清单

## 问题诊断

### 1. 管理员账号验证
**账号**: xiaoweihua / cqu
**验证方法**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"xiaoweihua","password":"cqu"}'
```

**期望结果**: 返回包含token的成功响应

### 2. 订单数据检查
**需要条件**:
- 数据库中存在状态为CREATED的订单
- 订单ID为1-30（对应30个并发线程）

**SQL查询**:
```sql
SELECT id, status, buyer_id, seller_id 
FROM orders 
WHERE status = 'CREATED' 
ORDER BY id 
LIMIT 30;
```

### 3. API接口验证
**发货接口**:
```bash
curl -X PUT http://localhost:8080/api/admin/orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"status":"SHIPPED"}'
```

**确认收货接口**:
```bash
curl -X PUT http://localhost:8080/api/admin/orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"status":"RECEIVED"}'
```

## 修复建议

### 方案1: 数据库准备
1. 确保有管理员用户: xiaoweihua
2. 创建30个CREATED状态的测试订单
3. 验证API接口可正常调用

### 方案2: 脚本优化
1. 添加更详细的错误日志
2. 增加管理员登录验证
3. 添加订单存在性检查

### 方案3: 简化测试
1. 先测试单个订单的状态转换
2. 验证成功后再进行并发测试
3. 使用实际存在的订单ID