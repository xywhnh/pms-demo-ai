# SkyWalking MCP 构建与配置指南

SkyWalking MCP 是一个 [Model Context Protocol](https://modelcontextprotocol.io/) 服务端，
用于将 AI 工具（Cursor、Claude Code、GitHub Copilot 等）与 SkyWalking OAP 对接，
实现通过 AI 直接查询链路追踪、日志、指标、拓扑、告警等可观测性数据。

## 前置条件

- [Go](https://go.dev/dl/) 1.23+（用于编译）
- [Git](https://git-scm.com/)
- SkyWalking OAP 10.2.0+（swmcp 使用 v2 GraphQL API）

## 构建步骤（Windows）

```powershell
# 1. 克隆源码
git clone https://github.com/apache/skywalking-mcp.git
cd skywalking-mcp

# 2. 确认 Go 版本
go version

# 3. 下载依赖
go mod tidy

# 4. 编译生成 swmcp.exe
go build -o .\swmcp.exe .\cmd\skywalking-mcp

# 5. 验证安装
.\swmcp.exe --help
```

## 构建步骤（Linux / macOS）

```bash
git clone https://github.com/apache/skywalking-mcp.git
cd skywalking-mcp

go version
go mod tidy
go build -o ./swmcp ./cmd/skywalking-mcp

./swmcp --help
```

## 运行模式

### stdio 模式（本地 MCP 客户端使用）

```bash
swmcp stdio --sw-url http://localhost:12800
```

### SSE 模式（远程 HTTP 服务）

```bash
swmcp sse --sse-address 0.0.0.0:8000 --base-path /mcp --sw-url http://localhost:12800
```

### Streamable 模式（HTTP 流式传输）

```bash
swmcp streamable --address 0.0.0.0:8000 --sw-url http://localhost:12800
```

## Cursor 配置

编辑 `~/.cursor/mcp.json`：

### 本地 stdio 模式

```json
{
  "mcpServers": {
    "skywalking": {
      "command": "D:\\code\\skywalking-mcp\\swmcp.exe",
      "args": [
        "stdio",
        "--sw-url", "http://<OAP_IP>:12800"
      ]
    }
  }
}
```

### 远程 SSE 模式

在远程服务器启动 SSE 服务后，本地 Cursor 配置：

```json
{
  "mcpServers": {
    "skywalking": {
      "url": "http://<SERVER_IP>:8000/mcp/sse"
    }
  }
}
```

## 常用命令参考

```
swmcp [command]

Available Commands:
  stdio       启动 stdio 模式（本地 MCP 客户端）
  sse         启动 SSE HTTP 服务
  streamable  启动 Streamable HTTP 服务

Global Flags:
  --sw-url string        OAP 地址（如 http://localhost:12800）
  --sw-username string   OAP 认证用户名（支持 ${ENV_VAR}）
  --sw-password string   OAP 认证密码（支持 ${ENV_VAR}）
  --sw-insecure          跳过 TLS 证书验证（仅开发环境）
  --log-file string      日志文件路径
  --log-level string     日志级别：debug/info/warn/error（默认 info）
  --read-only            只读模式
```

## 注意事项

- swmcp 需要与 OAP 版本匹配：v0.1.0+ 要求 OAP **10.2.0** 及以上
- 如果 OAP 版本低于 10.2.0，会出现 `Field 'queryTraces' is undefined` 错误
- Windows 下如果不想安装 Go，可在 Linux 服务器上编译后以 SSE 模式运行，本地通过 URL 连接
