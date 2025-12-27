# JMeter 压测脚本说明

本目录包含CQU抽象集市的JMeter压力测试脚本，用于验证系统在高并发场景下的性能和正确性。

## 前置条件

1. 安装 JMeter 5.x 或更高版本
2. 确保后端服务已启动并可访问
3. 根据实际环境修改测试脚本中的变量

## 测试场景

### 场景A：商品搜索压测

**文件**: `product-search-load-test.jmx`

**测试目标**: 验证商品搜索接口在高并发下的性能表现

**测试配置**:
- 100并发线程组（默认启用）
- 300并发线程组（默认禁用，需手动启用）
- 持续时间：1分钟
- 爬坡时间：10秒（100并发）/ 30秒（300并发）

**测试参数**:
- 随机关键词搜索
- 随机分类筛选
- 随机价格区间
- 随机排序方式

**运行命令**:
```bash
jmeter -n -t product-search-load-test.jmx -l product-search-results.jtl -e -o product-search-report
```

### 场景B：下单压测

**文件**: `order-create-load-test.jmx`

**测试目标**: 验证并发下单时的库存一致性（库存为1的商品只能被1个用户成功下单）

**测试配置**:
- 50并发线程组（默认启用）
- 100并发线程组（默认禁用，需手动启用）
- 使用同步定时器确保真正并发

**数据准备**:
1. 修改 `users.csv` 文件，填入真实的测试用户账号
2. 修改脚本中的 `PRODUCT_ID` 变量，指向一个状态为 ON_SALE 的商品

**验证指标**:
- 成功订单数应为1
- 其余请求应返回409（库存不足）

**运行命令**:
```bash
jmeter -n -t order-create-load-test.jmx -l order-create-results.jtl -e -o order-create-report
```

### 场景C：确认收货+评价压测

**文件**: `order-flow-load-test.jmx`

**测试目标**: 验证订单状态机在并发操作下的正确性

**测试配置**:
- 50并发线程组
- 完整流程：卖家发货 → 买家确认收货 → 买家评价

**数据准备**:
1. 修改 `orders.csv` 文件，填入真实的订单数据
2. 订单状态必须为 CREATED（待发货）
3. 确保买家和卖家账号正确

**验证指标**:
- 所有发货操作成功
- 所有确认收货操作成功
- 所有评价操作成功
- 状态转换符合状态机规则

**运行命令**:
```bash
jmeter -n -t order-flow-load-test.jmx -l order-flow-results.jtl -e -o order-flow-report
```

## 数据文件说明

### users.csv

用于下单压测的用户数据文件：

```csv
username,password
testuser1,password123
testuser2,password123
...
```

### orders.csv

用于确认收货+评价压测的订单数据文件：

```csv
orderId,buyerUsername,buyerPassword,sellerUsername,sellerPassword
1,buyer1,password123,seller1,password123
...
```

## 配置变量

所有脚本都支持以下变量配置：

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| BASE_URL | localhost | 后端服务地址 |
| PORT | 8080 | 后端服务端口 |
| PRODUCT_ID | 1 | 待抢购商品ID（场景B） |

## 测试报告

运行完成后，JMeter会生成以下报告文件：

- `*-aggregate-report.csv`: 聚合统计报告
- `*-results.csv`: 详细结果日志
- `*-summary.csv`: 汇总报告
- `*-report/`: HTML格式的可视化报告（使用 `-e -o` 参数生成）

## 注意事项

1. **数据准备**: 运行测试前，请确保测试数据（用户、商品、订单）已在数据库中创建
2. **环境隔离**: 建议在测试环境运行，避免影响生产数据
3. **资源监控**: 测试期间建议监控服务器CPU、内存、数据库连接等指标
4. **结果分析**: 关注响应时间、吞吐量、错误率等关键指标
5. **清理数据**: 测试完成后，根据需要清理测试产生的数据

## 常见问题

### Q: 如何切换100并发和300并发？
A: 在JMeter GUI中，右键点击线程组，选择"启用"或"禁用"

### Q: 如何修改测试持续时间？
A: 修改线程组的"持续时间"属性

### Q: 测试报告在哪里？
A: 使用 `-e -o` 参数运行后，报告会生成在指定目录中
