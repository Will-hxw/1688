#!/bin/bash

# ============================================
# CQU抽象集市后端API演示脚本
# 使用curl命令演示所有API功能
# ============================================
docker cp demo/demo-data.sql cqu-marketplace-mysql:/tmp/demo-data.sql
docker exec cqu-marketplace-mysql mysql -u root -proot --default-character-set=utf8mb4 cqu_marketplace -e "source /tmp/demo-data.sql"

BASE_URL="http://localhost:8081/api"
USER_TOKEN=""
ADMIN_TOKEN=""
SELLER_TOKEN=""
PRODUCT_ID=""
ORDER_ID=""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

print_title() {
    echo -e "\n${CYAN}=========================================="
    echo -e "    CQU抽象集市后端API演示"
    echo -e "==========================================${NC}\n"
}

print_section() {
    echo -e "\n${BLUE}------------------------------------------"
    echo -e " $1"
    echo -e "------------------------------------------${NC}\n"
}

print_step() {
    echo -e "${YELLOW}>>> $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

wait_key() {
    echo -e "\n${YELLOW}按任意键继续...${NC}"
    read -n 1 -s
}

print_title

# ============================================
# 1. 认证模块 /api/auth
# ============================================
print_section "1. 认证模块 /api/auth"

print_step "1.1 用户注册 POST /auth/register"
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testbuyer",
    "password": "password123",
    "nickname": "测试买家"
  }' | jq '.'

wait_key

print_step "1.2 用户登录（买家）POST /auth/login"
RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testbuyer",
    "password": "password123"
  }')
echo "$RESPONSE" | jq '.'
USER_TOKEN=$(echo "$RESPONSE" | jq -r '.data.token // empty')
[ -n "$USER_TOKEN" ] && print_success "买家Token已保存" || print_error "登录失败"

wait_key

print_step "1.3 卖家登录（zhangsan）POST /auth/login"
# 注意：demo-data.sql 中用户密码是 123456
RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "password": "123456"
  }')
echo "$RESPONSE" | jq '.'
SELLER_TOKEN=$(echo "$RESPONSE" | jq -r '.data.token // empty')
[ -n "$SELLER_TOKEN" ] && print_success "卖家Token已保存" || print_error "登录失败"

wait_key

print_step "1.4 管理员登录（xiaoweihua）POST /auth/login"
RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "xiaoweihua",
    "password": "cqu"
  }')
echo "$RESPONSE" | jq '.'
ADMIN_TOKEN=$(echo "$RESPONSE" | jq -r '.data.token // empty')
[ -n "$ADMIN_TOKEN" ] && print_success "管理员Token已保存" || print_error "登录失败"

wait_key

# ============================================
# 2. 商品模块 /api/products
# ============================================
print_section "2. 商品模块 /api/products"

print_step "2.1 搜索商品（公开）GET /products"
curl -s "$BASE_URL/products?keyword=iPhone&page=1&pageSize=5" | jq '.'

wait_key

print_step "2.2 按分类搜索 GET /products?category=电子产品"
# 注意：中文参数需要URL编码
curl -s "$BASE_URL/products?category=%E7%94%B5%E5%AD%90%E4%BA%A7%E5%93%81&page=1&pageSize=5" | jq '.'

wait_key

print_step "2.3 按价格区间搜索 GET /products?minPrice=100&maxPrice=500"
curl -s "$BASE_URL/products?minPrice=100&maxPrice=500&page=1&pageSize=5" | jq '.'

wait_key

print_step "2.4 获取商品详情（公开）GET /products/1"
curl -s "$BASE_URL/products/1" | jq '.'

wait_key

print_step "2.5 发布商品 POST /products（需登录）"
if [ -n "$SELLER_TOKEN" ]; then
    RESPONSE=$(curl -s -X POST "$BASE_URL/products" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $SELLER_TOKEN" \
      -d '{
        "name": "测试商品-二手键盘",
        "description": "机械键盘，青轴，使用半年，手感很好",
        "price": 199.00,
        "imageUrl": "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=400",
        "category": "电子产品",
        "stock": 1
      }')
    echo "$RESPONSE" | jq '.'
    PRODUCT_ID=$(echo "$RESPONSE" | jq -r '.data // empty')
    [ -n "$PRODUCT_ID" ] && print_success "商品发布成功，ID: $PRODUCT_ID" || print_error "发布失败"
else
    print_error "卖家未登录，跳过发布商品测试"
fi

wait_key

print_step "2.6 更新商品 PUT /products/$PRODUCT_ID"
if [ -n "$PRODUCT_ID" ] && [ -n "$SELLER_TOKEN" ]; then
    curl -s -X PUT "$BASE_URL/products/$PRODUCT_ID" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $SELLER_TOKEN" \
      -d '{
        "name": "测试商品-二手机械键盘（已更新）",
        "description": "机械键盘，青轴，使用半年，手感很好，送键帽",
        "price": 179.00,
        "imageUrl": "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=400",
        "category": "电子产品",
        "stock": 1
      }' | jq '.'
else
    print_error "商品ID或卖家Token为空，跳过更新商品测试"
fi

wait_key

# ============================================
# 3. 订单模块 /api/orders
# ============================================
print_section "3. 订单模块 /api/orders"

print_step "3.1 创建订单 POST /orders（需Idempotency-Key）"
IDEMPOTENCY_KEY="demo-$(date +%s)"
RESPONSE=$(curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -d '{
    "productId": 1
  }')
echo "$RESPONSE" | jq '.'
ORDER_ID=$(echo "$RESPONSE" | jq -r '.data // empty')
[ -n "$ORDER_ID" ] && print_success "订单创建成功，ID: $ORDER_ID" || print_error "创建失败"

wait_key

print_step "3.2 买家订单列表 GET /orders/buyer"
curl -s "$BASE_URL/orders/buyer?page=1&pageSize=10" \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.'

wait_key

print_step "3.3 卖家订单列表 GET /orders/seller"
if [ -n "$SELLER_TOKEN" ]; then
    curl -s "$BASE_URL/orders/seller?page=1&pageSize=10" \
      -H "Authorization: Bearer $SELLER_TOKEN" | jq '.'
else
    print_error "卖家未登录，跳过卖家订单列表测试"
fi

wait_key

print_step "3.4 卖家发货 POST /orders/{id}/ship"
# 使用演示数据中的CREATED订单（订单17，卖家是zhangsan/id=2）
if [ -n "$SELLER_TOKEN" ]; then
    curl -s -X POST "$BASE_URL/orders/17/ship" \
      -H "Authorization: Bearer $SELLER_TOKEN" | jq '.'
else
    print_error "卖家未登录，跳过发货测试"
fi

wait_key

print_step "3.5 买家确认收货 POST /orders/{id}/receive"
# 订单11的买家是周八(id=7)，不是testbuyer，此测试预期返回403
echo "注意：订单11的买家是周八，不是testbuyer，此测试预期返回403"
curl -s -X POST "$BASE_URL/orders/11/receive" \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.'

wait_key

print_step "3.6 取消订单 POST /orders/{id}/cancel"
if [ -n "$ORDER_ID" ]; then
    curl -s -X POST "$BASE_URL/orders/$ORDER_ID/cancel" \
      -H "Authorization: Bearer $USER_TOKEN" | jq '.'
fi

wait_key

# ============================================
# 4. 评价模块 /api/reviews
# ============================================
print_section "4. 评价模块 /api/reviews"

print_step "4.1 获取商品评价（公开）GET /products/4/reviews"
curl -s "$BASE_URL/products/4/reviews?page=1&pageSize=10" | jq '.'

wait_key

print_step "4.2 创建评价 POST /reviews（需RECEIVED状态订单）"
# 订单6的买家是陈三学姐(id=12)，不是testbuyer，此测试预期返回403
echo "注意：订单6的买家是陈三学姐，不是testbuyer，此测试预期返回403"
curl -s -X POST "$BASE_URL/reviews" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "orderId": 6,
    "rating": 5,
    "content": "商品很好，卖家发货快，推荐购买！"
  }' | jq '.'

wait_key

print_step "4.3 我的评价列表 GET /reviews/my"
curl -s "$BASE_URL/reviews/my?page=1&pageSize=10" \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.'

wait_key

# ============================================
# 5. 用户模块 /api/users
# ============================================
print_section "5. 用户模块 /api/users"

print_step "5.1 获取当前用户信息 GET /users/me"
curl -s "$BASE_URL/users/me" \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.'

wait_key

print_step "5.2 更新个人资料 PUT /users/me"
curl -s -X PUT "$BASE_URL/users/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "nickname": "测试买家（已更新）",
    "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=testbuyer"
  }' | jq '.'

wait_key

print_step "5.3 我的商品列表 GET /users/me/products"
if [ -n "$SELLER_TOKEN" ]; then
    curl -s "$BASE_URL/users/me/products?page=1&pageSize=10" \
      -H "Authorization: Bearer $SELLER_TOKEN" | jq '.'
else
    print_error "卖家未登录，跳过我的商品列表测试"
fi

wait_key

# ============================================
# 6. 文件上传 /api/upload
# ============================================
print_section "6. 文件上传 /api/upload"

print_step "6.1 上传图片 POST /upload"
# 创建一个临时测试图片（1x1像素的PNG）
TEST_IMAGE="/tmp/test_image_$$.png"
echo -n 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==' | base64 -d > "$TEST_IMAGE" 2>/dev/null

if [ -f "$TEST_IMAGE" ]; then
    curl -s -X POST "$BASE_URL/upload" \
      -H "Authorization: Bearer $USER_TOKEN" \
      -F "file=@$TEST_IMAGE" | jq '.'
    rm -f "$TEST_IMAGE"
else
    echo "无法创建测试图片，显示示例命令："
    echo 'curl -X POST "$BASE_URL/upload" \'
    echo '  -H "Authorization: Bearer $TOKEN" \'
    echo '  -F "file=@/path/to/image.jpg"'
fi

wait_key

# ============================================
# 7. 管理后台 /api/admin
# ============================================
print_section "7. 管理后台 /api/admin（需管理员权限）"

print_step "7.1 用户列表 GET /admin/users"
curl -s "$BASE_URL/admin/users?page=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'

wait_key

print_step "7.2 禁用用户 PUT /admin/users/{id}/disable"
# 禁用用户ID=9（郑十），不影响主要测试用户
curl -s -X PUT "$BASE_URL/admin/users/9/disable" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'

wait_key

print_step "7.3 启用用户 PUT /admin/users/{id}/enable"
# 立即重新启用，恢复数据
curl -s -X PUT "$BASE_URL/admin/users/9/enable" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'

wait_key

print_step "7.4 商品列表 GET /admin/products"
curl -s "$BASE_URL/admin/products?page=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'

wait_key

print_step "7.5 订单列表 GET /admin/orders"
curl -s "$BASE_URL/admin/orders?page=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'

wait_key

print_step "7.6 修改订单状态 PUT /admin/orders/{id}/status"
# 订单16是CREATED状态，可以改为SHIPPED
curl -s -X PUT "$BASE_URL/admin/orders/16/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"status": "SHIPPED"}' | jq '.'

wait_key

print_step "7.7 评价列表 GET /admin/reviews"
curl -s "$BASE_URL/admin/reviews?page=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'

wait_key

print_step "7.8 删除评价 DELETE /admin/reviews/{id}"
# 软删除评价ID=5（可恢复），不会真正丢失数据
curl -s -X DELETE "$BASE_URL/admin/reviews/5" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'
echo "注意：评价是软删除，数据库中 deleted=1，可通过SQL恢复"

wait_key

# ============================================
# 演示完成
# ============================================
print_section "演示完成"

echo -e "${GREEN}所有API功能演示完成！${NC}\n"
