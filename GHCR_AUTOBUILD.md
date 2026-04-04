# GitHub Actions 自动构建并发布到 GHCR

## 目标

- `service-a` 与 `service-b` 在 GitHub Actions 自动构建
- 自动推送到 `GitHub Container Registry (GHCR)`
- 服务器只负责 `pull + run`，不再本地构建镜像

## 已提供的文件

- `.github/workflows/build-and-push-ghcr.yml`
- `demo-service-a/Dockerfile.runtime`
- `demo-service-b/Dockerfile.runtime`
- `docker-compose.ghcr.yml`
- `scripts/up-ghcr.sh`
- `scripts/down-ghcr.sh`

## 镜像命名规则

- `ghcr.io/<owner>/pms-demo-ai-service-a:<tag>`
- `ghcr.io/<owner>/pms-demo-ai-service-b:<tag>`

其中 `<tag>` 默认会有：

- `latest`（仅默认分支）
- `sha-<short-sha>`
- `<branch-name>`（分支名）

## 自动化触发

- push 到 `main` 自动构建并推送
- 手工触发 `workflow_dispatch`

## 首次使用前检查

1. 仓库 `Actions` 功能已开启
2. 仓库有权限发布 package（workflow 已声明 `packages: write`）
3. 若服务器拉取私有镜像，准备带 `read:packages` 的 GitHub PAT

## 服务器侧运行（不构建）

```bash
cd /root/code/pms-demo-ai
git pull --rebase

# 私有镜像需要先登录 GHCR
docker login ghcr.io -u <github-username>

# 默认拉取 ghcr.io/xywhnh/*:latest
bash scripts/up-ghcr.sh
```

指定镜像 owner 或 tag：

```bash
GHCR_OWNER=xywhnh IMAGE_TAG=sha-<short-sha> bash scripts/up-ghcr.sh
```

停止服务：

```bash
bash scripts/down-ghcr.sh
```

## 验证接口

```bash
curl -s http://127.0.0.1:8081/api/demo/health
curl -s http://127.0.0.1:8082/api/scenario/normal
curl -s "http://127.0.0.1:8081/api/demo/execute?scenario=REMOTE_SUCCESS"
```
