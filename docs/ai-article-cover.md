# AI 文章封面

## 编辑流程

- 管理端文章编辑页填写标题和正文，点击「AI 生成」。
- 生成的是候选封面；预览后点击「采用封面」才会填入表单。
- 保存草稿或发布文章后，候选封面才与文章正式关联。
- 保存、发布以及无封面的文章均不会自动调用生图平台。
- 可以重新生成、取消、移除封面，或者继续上传自己的图片。

## 数据库升级

已有数据库部署前执行 `Backend/WuTao-server/src/main/resources/database/ai-cover-schema-upgrade.sql`，
连接时选中 `WuTao` 数据库。不要执行会删除数据库的完整初始化脚本。
全新部署的两份初始化 SQL 已包含 `ai_cover_assets` 表。

此表必须在部署新版后端前创建，即使暂时不启用生图。
已有图片一律不追溯为 AI 图片，不会被自动删除。

## 平台配置

平台与模型暂未指定，默认关闭，无需现在提供密钥。启用时：

1. 后端使用 `mvn -Pwith-ai package` 打包。
2. 本地配置参照 `application.yml.template`，将 `wutao.ai.image` 配置加入实际使用的配置文件。
3. Docker 配置参照 `docker/.env.example`。启用 `AI_IMAGE_ENABLED=true` 后重新构建后端镜像，会自动打包 AI 模块，不要求同时启用摘要。

```yaml
wutao:
  ai:
    image:
      enabled: ${AI_IMAGE_ENABLED:false}
      endpoint: ${AI_IMAGE_ENDPOINT:}
      api-key: ${AI_IMAGE_API_KEY:}
      model-name: ${AI_IMAGE_MODEL_NAME:}
      size: ${AI_IMAGE_SIZE:1536x1024}
      response-format: ${AI_IMAGE_RESPONSE_FORMAT:}
      timeout-seconds: ${AI_IMAGE_TIMEOUT_SECONDS:180}
```

现有适配器支持同步 `images/generations` JSON 协议：

- `endpoint` 是完整接口地址，不是聊天地址；生产必须使用可信 HTTPS 地址。
- 使用 Bearer 密钥认证；发送 `model`、`prompt`、`n=1` 和可选 `size`。
- 成功响应必须包含 `data[0].b64_json`，对应 PNG 或 JPEG 图片，最大 20MB、最大边长 4096 像素。
- `response-format` 按平台要求设置 `b64_json`，不支持此参数时留空。
- 不会下载响应中的 `url`，不会自动重试收费的生成请求。
- `size` 必须符合所选模型支持的尺寸；不需要该参数时设为空。
- 摘要的开关、聊天模型、密钥不会被用于生图。
- 仓库 Nginx 配置已为生图接口单独设置 300 秒等待时间；自定义反向代理也需要同步调整。

如果选定平台使用异步任务或其他协议，只需在 `WuTao-ai` 中替换 `AiCoverGenerator` 实现，
文章保存、候选确认、OSS 存储与清理流程无需变更。

## 图片清理

- AI 图片使用独立 OSS key，来源以数据库资产记录为准。
- 替换封面或删除文章时，同一事务内将旧 AI 图片标记为待清理；提交后异步删除 OSS 对象。
- 事务回滚不会删除图片。OSS 删除失败每五分钟重试，定时任务每分钟扫描最多 50 个。
- 未保存、未采用、生成中断的候选图片 24 小时后清理；取消预览时提前标记清理。
- 手动上传和外部图片不会被自动删除。AI 候选不可通过粘贴 URL 绕过确认，也不能跨文章复用。
- 前端展示签名预览 URL，数据库和表单保存不带签名的原始 URL。

## 验证

```shell
cd Backend
mvn test
mvn -Pwith-ai test
cd ../Frontend-Admin
pnpm build
```

后端测试覆盖未配置、候选生成、非法图片、归属与过期校验、人工采用、
草稿保存、文章删除后的提交时机，以及 OSS 清理失败重试。
平台适配器使用本地 HTTP 测试服务，不会调用收费平台。

管理端浏览器回归使用模拟接口，验证生成、重新生成、取消、采用、未保存提示、
重复保存草稿、替换候选、人工上传、移除封面、平台未配置、错误重试及手机端布局。
本次未执行真实数据库升级，未进行真实生图平台与 OSS 联调，也未重启现有后端。
本机 Docker 引擎未启动，Nginx 配置尚未进行容器内语法验证。
