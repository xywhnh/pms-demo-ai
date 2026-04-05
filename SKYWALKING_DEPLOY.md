# SkyWalking 部署与集成（Docker Compose）

## 目标

- 部署 `skywalking-oap` + `skywalking-ui`
- 将 `service-a`、`service-b` 接入 SkyWalking Java Agent
- 在 SkyWalking UI 中查看服务拓扑、调用链和指标

## 文件说明

- `docker-compose.skywalking.yml`：SkyWalking 叠加编排（基于现有 `docker-compose.yml`）
- `scripts/setup-skywalking-agent.sh`：下载并安装 SkyWalking Java Agent
- `scripts/up-skywalking.sh`：一键拉取并启动（含 service-a/service-b + SkyWalking）
- `scripts/down-skywalking.sh`：一键停止

## 一键启动

```bash
cd /root/code/pms-demo-ai
git pull --rebase
bash scripts/up-skywalking.sh
```

启动后访问：

- SkyWalking UI: `http://<服务器IP>:18080`
- OAP HTTP API: `http://<服务器IP>:12800`
- OAP gRPC: `<服务器IP>:11800`

## 触发调用链数据

```bash
curl -s "http://127.0.0.1:8081/api/demo/execute?scenario=REMOTE_SUCCESS"
curl -s "http://127.0.0.1:8081/api/demo/execute?scenario=REMOTE_TIMEOUT"
```

然后在 UI 的 `Services`、`Trace`、`Topology` 页面查看数据。

## 停止

```bash
bash scripts/down-skywalking.sh
```

## 运维建议（最佳实践）

1. 生产环境建议将 OAP 存储从默认 `h2` 切换到 `elasticsearch/opensearch`。
2. 建议仅对测试环境开启 `seccomp=unconfined`，生产环境优先升级 Docker/内核后恢复默认 seccomp。
3. 建议固定镜像 tag（例如 `sha-xxxxxxx`），避免 `latest` 漂移。
4. 建议将 `18080`、`11800`、`12800` 仅对可信网络开放。
