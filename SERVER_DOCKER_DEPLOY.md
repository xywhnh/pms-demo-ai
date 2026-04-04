# 服务器 Docker 部署清单（service-a + service-b）

## 1. 前置条件

在服务器上确认以下条件：

1. 已安装 Docker（含 `docker compose` 插件）
2. 服务器可访问 GitHub（用于拉取代码）
3. 防火墙/安全组放通端口：`8081`、`8082`

检查命令：

```bash
docker --version
docker compose version
```

## 2. 首次部署（从代码到启动）

```bash
# 1) 拉代码
git clone https://github.com/xywhnh/pms-demo-ai.git
cd pms-demo-ai

# 2) 一键部署（拉最新代码 + 构建镜像 + 启动）
bash scripts/deploy.sh
```

> `scripts/deploy.sh` 会自动执行：
> - `git pull --rebase`（如果是 Git 工作目录）
> - `docker compose build`
> - `docker compose up -d --remove-orphans`

## 3. 日常更新部署

后续每次发布新代码，只需要：

```bash
cd pms-demo-ai
bash scripts/deploy.sh
```

## 4. 服务访问方式

假设服务器 IP 为 `X.X.X.X`：

- service-a 健康检查：`http://X.X.X.X:8081/api/demo/health`
- service-b 正常场景：`http://X.X.X.X:8082/api/scenario/normal`
- service-a 调用 service-b：`http://X.X.X.X:8081/api/demo/execute?scenario=REMOTE_SUCCESS`

## 5. 验证脚本

```bash
# 本机验证
bash scripts/check.sh

# 指定服务器IP验证
bash scripts/check.sh X.X.X.X
```

## 6. 运维常用命令

```bash
# 查看状态
docker compose ps

# 查看日志
docker compose logs -f service-a
docker compose logs -f service-b

# 停止服务
bash scripts/stop.sh

# 停止并删除容器/网络
docker compose down
```

## 7. 端口放通示例（Linux）

### Ubuntu / Debian（UFW）

```bash
sudo ufw allow 8081/tcp
sudo ufw allow 8082/tcp
sudo ufw reload
sudo ufw status
```

### CentOS / RHEL（firewalld）

```bash
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

## 8. 关键说明

1. `service-a` 在容器中通过 `SERVICE_B_BASE_URL=http://service-b:8082` 调用 `service-b`，已在 `docker-compose.yml` 配置完成。
2. 两个服务日志会写入宿主机目录：
   - `./logs/service-a/`
   - `./logs/service-b/`
3. 如果服务器是云主机，还需要在云平台安全组放通 `8081`、`8082`。

