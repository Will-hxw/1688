# 前端构建阶段
FROM node:20-alpine AS frontend-builder

WORKDIR /app

# 复制前端项目
COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# Nginx运行阶段
FROM nginx:alpine

# 删除默认配置，使用自定义配置
RUN rm -f /etc/nginx/conf.d/default.conf

# 复制nginx配置
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# 复制前端构建产物
COPY --from=frontend-builder /app/dist /usr/share/nginx/html

# 创建上传目录
RUN mkdir -p /var/www/uploads

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
