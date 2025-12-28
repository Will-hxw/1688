# JMeter 压测脚本

CQU抽象集市压力测试，包含两个核心场景。

## 前置条件

1. 安装 JMeter 5.x+
2. 后端服务已启动（默认 localhost:8081）

## 场景一：注册压测

**文件**: `register-load-test.jmx`

**测试目标**: 验证注册接口在高并发下的性能

**配置**:
- 100并发（默认启用）- 持续60秒

**运行命令**:
```bash
jmeter -n -t register-load-test.jmx -l register-results.jtl -e -o register-report -f
```

## 场景二：下单压测

**文件**: `order-load-test.jmx`

**测试目标**: 验证并发下单时的库存一致性（库存为1的商品只能被1个用户成功下单）

**配置**:
- 50并发（默认启用）
- 100并发（默认禁用）
- 使用同步定时器确保真正并发

**数据准备**:
1. 修改 `users.csv`，填入测试用户账号密码
2. 修改脚本中的 `PRODUCT_ID` 变量，指向库存为1的在售商品

**验证指标**:
- 成功订单数应为1
- 其余请求应返回409（库存不足）

**运行命令**:
```bash
jmeter -n -t order-load-test.jmx -l order-results.jtl -e -o order-report -f
```

## 配置变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| BASE_URL | localhost | 后端服务地址 |
| PORT | 8081 | 后端服务端口 |
| PRODUCT_ID | 1 | 待抢购商品ID（下单压测） |

## 数据文件

### users.csv
```csv
username,password
zhangsan,123456
lisi,123456
...
```

## 注意事项

1. 下单压测前确保商品库存为1且状态为ON_SALE
2. 测试完成后检查数据库，验证只有1条成功订单
3. 切换并发数：在JMeter GUI中启用/禁用对应线程组
