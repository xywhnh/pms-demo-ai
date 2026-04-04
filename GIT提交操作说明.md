# pms-demo-ai 提交到 Git 仓库操作说明

## 1. 当前工程目录梳理（哪些该提交，哪些不该提交）

应提交（源码与配置）：

- `pom.xml`（父工程）
- `demo-service-a/src/**`、`demo-service-b/src/**`
- `demo-service-a/pom.xml`、`demo-service-b/pom.xml`
- `.mvn/wrapper/maven-wrapper.properties`、`mvnw`、`mvnw.cmd`
- `openspec/**`（如果你希望把需求/设计说明纳入版本管理）
- 说明文档（如 `HELP.md`、本文件）

不应提交（已放入 `.gitignore`）：

- 所有构建产物：`target/`、`**/target/`、`build/`、`out/`、`*.class`
- 运行日志：`logs/`、`*.log`
- IDE 临时文件：`.idea/`、`.vscode/`、`.qoder/`、`*.iml` 等
- 系统临时文件：`.DS_Store`、`Thumbs.db`、`*.tmp`、`*.swp`

## 2. 首次提交到 Git（当前目录还未初始化）

在工程根目录 `d:\code\java-demo\pms-demo-ai` 执行：

```powershell
git init
git add .
git status
git commit -m "chore: initialize pms-demo-ai multi-module project"
```

建议在 `git add .` 后先看 `git status`，确认没有 `target/`、`logs/`、`.idea/` 等无关文件。

## 3. 关联远端仓库并推送

先在 GitHub/GitLab 创建一个空仓库（不要勾选 README/.gitignore 初始化），然后执行：

```powershell
git branch -M main
git remote add origin <你的远端仓库URL>
git push -u origin main
```

示例远端 URL：

- HTTPS：`https://github.com/<org-or-user>/pms-demo-ai.git`
- SSH：`git@github.com:<org-or-user>/pms-demo-ai.git`

## 4. 日常提交流程

```powershell
git pull --rebase
git add .
git status
git commit -m "feat: <本次变更说明>"
git push
```

## 5. 常见问题排查

1. 忽略规则不生效  
如果文件在加入 `.gitignore` 之前已经被 Git 跟踪，需要先取消跟踪再提交：

```powershell
git rm -r --cached target demo-service-a/target demo-service-b/target logs .idea .vscode
git add .
git commit -m "chore: remove generated files from git tracking"
```

2. 检查某个文件为什么被忽略

```powershell
git check-ignore -v <文件路径>
```

3. 首次提交前快速自检

```powershell
git status --short
```

确认只包含源码、配置和文档，不包含编译产物和本地环境文件。

