# 项目启动指南

本文用于通过 Docker Compose 启动完整项目。更详细的部署、HTTPS 和故障排查说明见 [docker/README.md](docker/README.md)。

## 1. 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- 构建前端时需要 Node.js 20.19+ 或 22.12+、pnpm 11+
- 首次构建需要能够访问 Maven、npm 和 Docker 镜像仓库

在仓库根目录确认环境：

```bash
docker --version
docker compose version
node --version
pnpm --version
```

## 2. 创建私有环境变量

Linux/macOS：

```bash
cp docker/.env.example .env
```

Windows PowerShell：

```powershell
Copy-Item docker/.env.example .env
```

编辑根目录 `.env`，至少填写以下配置：

```dotenv
MYSQL_ROOT_PASSWORD=
MYSQL_PASSWORD=
JWT_SECRET_KEY=

ALIOSS_ENDPOINT=
ALIOSS_ACCESS_KEY_ID=
ALIOSS_ACCESS_KEY_SECRET=
ALIOSS_BUCKET_NAME=

MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
EMAIL_PERSONAL=
EMAIL_FROM=

VISITOR_VERIFY_CODE=
```

MySQL root 密码、业务密码和 JWT 密钥应分别生成，不要复用。可使用以下命令生成随机值：

```bash
openssl rand -base64 32
```

`.env` 包含真实密钥，已被 Git 忽略。不要使用 `git add -f .env` 强制提交。

配置完成后进行校验。`--quiet` 可避免把解析后的密钥打印到终端：

```bash
docker compose --env-file .env config --quiet
```

## 3. 构建前端

分别安装依赖并构建四个前端：

```bash
pnpm --dir Frontend-Home install
pnpm --dir Frontend-Home build

pnpm --dir Frontend-Blog install
pnpm --dir Frontend-Blog build

pnpm --dir Frontend-Cv install
pnpm --dir Frontend-Cv build

pnpm --dir Frontend-Admin install
pnpm --dir Frontend-Admin build
```

将构建生成的 `dist` 内容放入对应目录：

| 前端模块 | Nginx 静态文件目录 |
| --- | --- |
| `Frontend-Home/dist` | `docker/html/wutao.cc/html` |
| `Frontend-Blog/dist` | `docker/html/blog.wutao.cc/html` |
| `Frontend-Cv/dist` | `docker/html/cv.wutao.cc/html` |
| `Frontend-Admin/dist` | `docker/html/admin.wutao.cc/html` |

如果使用自己的域名，需要同时修改 `docker/nginx/sites-enabled/wutao.cc` 中的 `server_name`、`root`，以及 `.env` 中的 `WEBSITE_*` 地址。

## 4. 启动服务

首次启动或后端代码发生变化时：

```bash
docker compose up -d --build
```

后续直接启动：

```bash
docker compose up -d
```

启动的服务包括：

- MySQL 8.0
- Redis 7
- Spring Boot 后端
- Nginx

## 5. 检查启动结果

```bash
docker compose ps
docker compose logs --tail=100 backend
curl http://127.0.0.1:5922/health
```

所有容器应处于运行状态，MySQL 和 Redis 应显示健康。后端健康检查接口应返回成功响应。

如需持续查看日志：

```bash
docker compose logs -f backend
```

## 6. 停止或更新

停止服务但保留 MySQL、Redis 数据：

```bash
docker compose down
```

只重新构建后端：

```bash
docker compose up -d --build backend
```

更新前端后，重新复制对应 `dist` 内容并重载 Nginx：

```bash
docker compose exec nginx nginx -s reload
```

## 7. 注意事项

- MySQL 初始化脚本只在 `mysql_data` 数据卷首次创建时执行。
- 已存在数据卷时，仅修改 `.env` 中的 MySQL 密码不会自动修改数据库用户密码。
- 不要执行 `docker compose down -v`，该命令会删除 MySQL 和 Redis 数据卷。
- 生产环境不要向公网开放 `3306` 和 `6379`，应通过防火墙或 Compose 端口绑定限制访问。
- OSS 建议使用最小权限 RAM 用户，不要使用阿里云主账号 AccessKey。
- 启用 AI 摘要时，在 `.env` 中设置 `AI_ENABLED=true` 和 `AI_API_KEY`，然后重新构建后端镜像。

## 8. 常见问题

Compose 提示 `required variable ... is missing a value`：检查根目录 `.env` 是否存在，以及对应变量是否为空。

后端启动失败：

```bash
docker compose logs backend
docker compose logs mysql
```

Nginx 返回 502：

```bash
docker compose ps
docker compose exec nginx nginx -t
```
