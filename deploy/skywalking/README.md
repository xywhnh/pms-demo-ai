# SkyWalking 部署与集成（Docker Compose）

## 目标

- 部署 `skywalking-oap` (v10.4.0) + `skywalking-ui`
- 将 `service-a`、`service-b` 接入 SkyWalking Java Agent
- 在 SkyWalking UI 中查看服务拓扑、调用链和指标
- 通过 SkyWalking MCP 对接 AI 工具（Cursor 等）

## 文件说明

```
项目根目录/
├── docker-compose.yml              # 应用服务部署（service-a/b，连接外部 OAP）
├── skywalking/agent/               # SkyWalking Java Agent（需手动下载，已 gitignore）
└── deploy/skywalking/
    ├── docker-compose.yml          # SkyWalking OAP + UI 独立部署
    ├── skywalking-mcp/             # SkyWalking MCP 构建与配置
    │   └── README.md               # MCP 构建指南、运行模式、Cursor 配置
    └── README.md                   # 本文档
```

## 使用方式

### 启动 SkyWalking（OAP + UI）

```bash
cd deploy/skywalking
docker compose up -d
```

### 启动应用服务（service-a + service-b）

```bash
# 从项目根目录执行
docker compose up -d
```

### 安装 SkyWalking Java Agent

应用服务通过 volume 挂载 `skywalking/agent/` 目录加载 Agent。首次使用需手动下载：

```bash
# 下载并解压到项目根目录的 skywalking/agent/
curl -fL https://archive.apache.org/dist/skywalking/java-agent/9.4.0/apache-skywalking-java-agent-9.4.0.tgz -o /tmp/skywalking-agent.tgz
mkdir -p skywalking/agent
tar -xzf /tmp/skywalking-agent.tgz -C /tmp/
cp -r /tmp/skywalking-java-agent/skywalking-agent/. skywalking/agent/
```

安装后需重启应用服务容器：

```bash
docker compose down && docker compose up -d
```

### 停止服务

```bash
# 停止 SkyWalking
cd deploy/skywalking && docker compose down

# 停止应用服务
docker compose down
```

## 访问地址

| 服务 | 地址 |
|------|------|
| SkyWalking UI | `http://<SERVER_IP>:18080` |
| OAP HTTP API | `http://<SERVER_IP>:12800` |
| OAP gRPC | `<SERVER_IP>:11800` |

## 触发调用链数据

```bash
curl -s "http://127.0.0.1:8081/api/demo/execute?scenario=REMOTE_SUCCESS"
curl -s "http://127.0.0.1:8081/api/demo/execute?scenario=REMOTE_TIMEOUT"
```

然后在 UI 的 `Services`、`Trace`、`Topology` 页面查看数据。

## SkyWalking MCP 集成

SkyWalking MCP 可将 AI 工具与 SkyWalking 对接，实现通过 AI 查询链路、日志、指标等数据。

详细的构建步骤、运行模式和 Cursor 配置请参考：[skywalking-mcp/README.md](skywalking-mcp/README.md)

## 运维建议

1. 生产环境建议将 OAP 存储从默认 `h2` 切换到 `elasticsearch/opensearch`。
2. 建议仅对测试环境开启 `seccomp=unconfined`，生产环境优先升级 Docker/内核后恢复默认 seccomp。
3. 建议固定镜像 tag，避免 `latest` 漂移。
4. 建议将 `18080`、`11800`、`12800` 仅对可信网络开放。
5. swmcp 需要与 OAP 版本匹配（v0.1.0+ 需要 OAP 10.2.0+）。
