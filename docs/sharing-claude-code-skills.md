# Claude Code Skills：让 AI 成为你的领域专家

> 分享主题：如何通过 Skill 将 Claude Code 从通用助手打造成垂直领域的诊断专家
> 演示项目：SkyWalking Trace Diagnoser — 微服务故障智能诊断

---

## 一、从一个真实问题说起

> "线上项目仪表盘接口响应慢，用户反馈加载要等好几秒。代码看着没问题，Service A 调了 Service B，Service B 也很快返回了，到底慢在哪？"

这是一个典型的分布式系统问题——**症状和根因不在同一个服务**。

传统排查路径：打开 SkyWalking UI → 找到对应 trace → 逐个展开 span → 对比代码 → 定位问题。这个过程对于经验丰富的工程师可能需要 30 分钟，对于不熟悉系统的人可能需要数小时。

**如果有一个"专家"，只需要你告诉它"仪表盘接口很慢"，它就能自动查 trace、分析 span 树、定位到具体代码行，并给出修复建议呢？**

这就是 Skill 的价值。

---

## 二、什么是 Skill？

### 2.1 一句话定义

**Skill 是一份写给 Claude 的"操作手册"**——它告诉 Claude 在特定场景下应该怎么思考、查什么数据、用什么工具、按什么格式输出。

### 2.2 类比理解

| 概念 | 类比 |
|------|------|
| **Claude Code 原生能力** | 一个聪明的应届毕业生——什么都能做，但不了解你的业务 |
| **CLAUDE.md** | 新员工入职手册——项目背景、代码规范、常见约定 |
| **Skill** | 一份标准作业流程（SOP）——遇到特定问题，按这个步骤排查 |
| **MCP Server** | 工具箱——给 Claude 提供可以调用的外部工具（如查 trace、查日志） |

**Skill + MCP = SOP + 工具箱 = 领域专家**

### 2.3 Skill 不是什么

- 不是插件或二进制程序——它就是 Markdown 文件
- 不是固定的脚本——Claude 会根据实际情况灵活应用
- 不需要编程——写清楚"什么时候用、怎么用、输出什么"就行

---

## 三、Skill 的组成结构

### 3.1 最简结构

```
.claude/skills/my-skill/
└── SKILL.md          # 唯一必需的文件
```

一个 `SKILL.md` 就是一个完整的 Skill。

### 3.2 完整结构（以本项目为例）

```
skills/skywalking-trace-diagnoser/
├── SKILL.md                        # 核心：工作流程 + 决策规则
├── agents/
│   └── openai.yaml                 # Agent 接口定义（可选）
└── references/
    ├── scenario-playbook.md        # 已知场景的 trace 特征库
    ├── code-map.md                 # 端点 → 代码文件映射表
    ├── mcp-contract.md             # MCP 接口能力清单
    └── analysis-template.md        # 诊断报告输出模板
```

### 3.3 SKILL.md 核心要素

```yaml
---
name: skywalking-trace-diagnoser           # Skill 名称
description: >-                             # 触发描述（Claude 据此判断何时启用）
  Analyze API failures by querying SkyWalking traces and logs
  through MCP, then correlate evidence with local Java code
  for root-cause diagnosis.
---

# 工作流程（Workflow）
1. 明确目标 → 2. 查询证据 → 3. 关联代码 → 4. 输出诊断

# 决策规则（Decision Rules）
- 不能仅凭返回码下结论，必须看 span + 日志
- 区分根因和传播路径
- ...

# 引用资料（References）
- scenario-playbook.md
- code-map.md
- ...
```

### 3.4 关键 Frontmatter 字段速查

| 字段 | 作用 | 示例 |
|------|------|------|
| `name` | Skill 名称，用于 `/name` 调用 | `skywalking-trace-diagnoser` |
| `description` | Claude 判断何时自动触发的依据 | `Analyze API failures...` |
| `disable-model-invocation` | 设为 `true` 则只能手动调用 | 部署类 Skill 建议设为 true |
| `allowed-tools` | Skill 激活时自动授权的工具 | `Read Grep Bash` |
| `context` | 设为 `fork` 则在独立子 Agent 中运行 | 适合重型分析任务 |
| `paths` | 限制只在匹配的文件路径下自动触发 | `src/backend/**` |

---

## 四、Skill 能做什么？

### 4.1 四大能力象限

```
                    被动触发                  主动调用
              ┌──────────────────┬──────────────────┐
  知识注入     │  背景知识         │  查询分析         │
              │  代码规范          │  /diagnose        │
              │  架构约定          │  /explain-code    │
              │  领域术语          │  /review-pr 123   │
              ├──────────────────┼──────────────────┤
  动作执行     │  自动修复         │  工作流自动化      │
              │  自动格式化        │  /deploy prod     │
              │  自动补充测试      │  /batch migrate   │
              │                  │  /loop 5m check   │
              └──────────────────┴──────────────────┘
```

### 4.2 内置 Skill 示例

| Skill | 功能 | 典型用法 |
|-------|------|---------|
| `/batch` | 大规模并行代码修改 | `/batch 把所有 var 替换为 const` |
| `/simplify` | 代码质量审查 | 修改代码后自动触发审查 |
| `/claude-api` | Claude API 参考 | 写 Anthropic SDK 代码时自动加载 |
| `/loop` | 定时轮询 | `/loop 5m 检查部署状态` |

### 4.3 自定义 Skill 场景

- **代码审查专家**：按团队规范审查 PR，输出标准化审查意见
- **数据库迁移助手**：根据 Schema 变更自动生成迁移脚本
- **故障诊断专家**：查 trace、查日志、关联代码、输出诊断报告 ← 今天的演示
- **API 文档生成器**：读代码自动生成 OpenAPI 文档
- **安全审计员**：检查代码中的安全隐患，按 OWASP Top 10 分类

---

## 五、Skill 与 Agent 的关系

### 5.1 核心区别

```
┌─────────────────────────────────────────────────────┐
│                   Claude Code 会话                    │
│                                                      │
│  ┌─────────┐    ┌─────────┐    ┌─────────────────┐ │
│  │ Skill A │    │ Skill B │    │   Sub-Agent     │ │
│  │(内联执行)│    │(内联执行)│    │ (独立上下文)    │ │
│  │共享会话  │    │共享会话  │    │ 看不到主会话    │ │
│  │上下文    │    │上下文    │    │ 只返回结果     │ │
│  └─────────┘    └─────────┘    └─────────────────┘ │
│       ↑               ↑              ↑              │
│       │               │              │              │
│  自动触发/手动    自动触发/手动    Claude 委派        │
└─────────────────────────────────────────────────────┘
```

| 维度 | Skill | Sub-Agent |
|------|-------|-----------|
| 上下文 | 共享主会话上下文 | 独立隔离的上下文 |
| 调用方式 | `/name` 或自动触发 | Claude 根据任务描述自动委派 |
| 适用场景 | 注入知识、编排流程 | 长任务、并行任务、保护主上下文 |
| 状态 | 能看到对话历史 | 从零开始，只带任务描述 |

### 5.2 协作模式

Skill 可以通过 `context: fork` 在 Sub-Agent 中运行，两者并非互斥：

```yaml
---
name: deep-analysis
context: fork        # 在独立 Sub-Agent 中执行
agent: Explore       # 使用 Explore 类型的 Agent
---
```

**最佳实践**：
- 轻量查询 / 知识注入 → Skill 内联执行
- 重型分析 / 并行任务 → Skill + `context: fork`
- 纯后台任务 → Sub-Agent

---

## 六、Skill 与 MCP 的协作

### 6.1 MCP 是什么？

**Model Context Protocol**——让 AI 调用外部工具的标准协议。

```
┌──────────┐     ┌──────────────┐     ┌─────────────────┐
│ Claude   │────→│  MCP Server  │────→│  SkyWalking OAP │
│ Code     │←────│  (桥接层)     │←────│  (可观测平台)    │
└──────────┘     └──────────────┘     └─────────────────┘
     │
     │ Skill 告诉 Claude：
     │ "用 MCP 查 trace，然后..."
     │
```

### 6.2 Skill + MCP = 领域专家

| 单独使用 | 效果 |
|---------|------|
| 只有 MCP | Claude 能查数据，但不知道查什么、怎么分析 |
| 只有 Skill | Claude 知道流程，但没有数据可查 |
| **Skill + MCP** | **Claude 知道查什么 + 能查到 + 会分析 = 专家** |

### 6.3 本项目的 MCP 能力

```
SkyWalking MCP Server 提供：
├── 按服务/端点/状态查询 trace 列表
├── 按 traceId 获取完整 span 树
├── 按 traceId 或时间窗口查询关联日志
└── 查询服务和端点目录
```

Skill 的 `mcp-contract.md` 定义了如何使用这些能力：

```markdown
## Required Capabilities
1. Query endpoint-level traces → find_traces_by_endpoint(...)
2. Query full trace details → get_trace_detail(...)
3. Query correlated logs → query_trace_logs(...)
4. Query service catalog → list_services_or_endpoints(...)
```

---

## 七、实战演示：SkyWalking Trace Diagnoser

### 7.1 演示环境

```
┌─────────────────────────────────────────────┐
│           微服务项目管理系统                    │
│                                              │
│  Service A (8081)     API Gateway / BFF     │
│       ↓          ↘                          │
│  Service B (8082)   Service C (8083)        │
│  任务管理服务        用户 & 通知平台服务       │
│       ↓                                      │
│  Service C (8083)                            │
│                                              │
│  SkyWalking OAP ← Agent 采集 trace + logs   │
│  SkyWalking MCP ← Claude Code 查询接口      │
└─────────────────────────────────────────────┘
```

### 7.2 五个隐藏问题场景

这些 API 看起来完全正常，问题隐藏在跨服务调用的运行时行为中：

#### 场景 1：项目仪表盘加载缓慢（N+1 远程调用）

```bash
# 触发
curl http://localhost:8081/api/projects/1/dashboard
# 现象：响应慢（750ms+），但返回 200
```

**问题隐藏在哪**：`ProjectService.enrichTasksWithAssigneeInfo()` 对 15 个任务逐一调用 Service C 查用户信息，每次 50ms，串行累计 750ms+。代码看起来就是一个普通的 for 循环，毫无异常。

#### 场景 2：创建任务成功但通知丢失（静默吞错）

```bash
# 触发
curl -X POST http://localhost:8081/api/projects/1/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"修复登录超时","assigneeId":5,"priority":"HIGH"}'
# 现象：返回 200 成功，但指派人收不到通知
```

**问题隐藏在哪**：Service C 的通知模板缺少 `HIGH` 优先级映射导致 NPE，但 Service B 用 try-catch 静默捕获了异常，只打了一行 `log.warn`。三个服务的代码各自看起来都没问题。

#### 场景 3：项目概览偶尔超时（级联超时）

```bash
# 触发
curl http://localhost:8081/api/projects/1/overview?includeStats=true
# 现象：504 超时
```

**问题隐藏在哪**：Service B 的统计算法是 O(n²)，耗时 4s+。Service A 用 `CompletableFuture.allOf()` 并行调用 B 和 C，但 allOf 要求全部完成——B 的 4s 拖垮了整个请求，即使 C 只要 60ms。

#### 场景 4：批量导入部分失败原因不明（错误信息丢失）

```bash
# 触发（其中 assigneeId 999 和 888 不存在）
curl -X POST http://localhost:8081/api/projects/1/tasks/import \
  -H "Content-Type: application/json" \
  -d '{"projectId":1,"tasks":[
    {"title":"任务1","assigneeId":3},
    {"title":"任务2","assigneeId":999},
    {"title":"任务3","assigneeId":5},
    {"title":"任务4","assigneeId":888}
  ]}'
# 现象：200 OK, {"success":2,"failed":2} — 但不知道哪条失败、为什么
```

**问题隐藏在哪**：Service B 的批量处理 catch 块只做了 `failCount++`，丢弃了具体的错误信息。只有 trace 的 span 树能看到具体是哪个 userId 校验失败。

#### 场景 5：完成任务触发重复通知（重试风暴）

```bash
# 触发
curl -X POST http://localhost:8081/api/projects/1/tasks/1/complete
# 现象：200 OK，但指派人收到 2-3 条重复通知
```

**问题隐藏在哪**：Service B 的 `ServiceCClient` 对通知调用配置了 2s 超时 + 重试 2 次，但 Service C 处理 `TASK_COMPLETED` 需要 2.5s（多渠道推送）。每次都超时触发重试，但 C 其实每次都处理完了——结果发了 3 次通知。重试逻辑在客户端配置层，不在业务代码中。

### 7.3 对比演示：不使用 Skill vs 使用 Skill

#### 不使用 Skill 的排查过程

```
你: "仪表盘接口很慢，帮我看看"

Claude: "让我看一下代码..."
  → 读 ProjectController.java
  → 读 ProjectService.java
  → 读 TaskServiceClient.java
  → 读 PlatformServiceClient.java
  → "代码逻辑看起来是正常的，getDashboard 方法先调 B 获取任务，
     然后调 C 获取用户信息... 你能提供更多信息吗？请求大概多慢？"

你: "大概 800ms"

Claude: "让我查看 enrichTasksWithAssigneeInfo 方法... 
  这里有一个 for 循环对每个 task 调用 getUser...
  不过每次调用应该很快才对。你能看一下 Service C 的日志吗？"

你: "..."
```

**问题**：
- Claude 只能读代码，看不到运行时行为
- 需要多轮对话，人工提供线索
- 依赖工程师的经验判断
- 可能走弯路（比如去查 Service C 为什么慢，其实 C 不慢）

#### 使用 Skill 的排查过程

```
你: "/skywalking-trace-diagnoser 仪表盘接口响应慢 
     GET /api/projects/1/dashboard"

Claude (Skill 自动编排):
  1. [MCP] 查询最近 10 分钟 /api/projects/1/dashboard 的慢 trace
  2. [MCP] 获取最慢 trace 的完整 span 树
  3. [发现] 1 个 Exit span 到 service-b (45ms) + 15 个 Exit span 到 service-c (各 ~50ms, 串行)
  4. [代码关联] code-map.md → ProjectService.java → enrichTasksWithAssigneeInfo()
  5. [模式匹配] scenario-playbook.md → N+1 远程调用模式
  6. [输出诊断报告]
```

**输出**：

```markdown
## 1) Incident Summary
- Endpoint: GET /api/projects/1/dashboard
- User symptom: 响应耗时 820ms
- Final status: 200 OK (功能正常，性能异常)

## 2) Evidence
- Trace id: abc123def456
- Span path: service-a → service-b (45ms) → service-a → service-c ×15 (串行, 共 750ms)

## 3) Root Cause
- Location: ProjectService.java:enrichTasksWithAssigneeInfo()
- 对每个 task 的 assigneeId 逐一调用 platformServiceClient.getUser()
- 15 个任务 = 15 次串行 HTTP 调用 = 750ms

## 4) Fix Direction
- 使用 GET /api/users/batch?ids=3,7,12,... 替换循环调用
- 预期延迟从 750ms 降至 60ms
```

#### 核心对比

| 维度 | 不使用 Skill | 使用 Skill |
|------|-------------|-----------|
| **数据来源** | 只能读代码 | 代码 + trace + 日志 |
| **排查时间** | 多轮对话 10-30 分钟 | 一次调用 30 秒 |
| **准确性** | 依赖人工经验推断 | 基于 trace 证据的精确定位 |
| **输出质量** | 口语化讨论 | 结构化诊断报告 |
| **可复现** | 每次排查路径不同 | 标准化流程，结果一致 |
| **门槛** | 需要熟悉系统的资深工程师 | 任何人都能触发诊断 |

---

## 八、如何创建自己的 Skill

### 8.1 三步上手

**第一步：创建文件**

```bash
mkdir -p .claude/skills/my-skill
touch .claude/skills/my-skill/SKILL.md
```

**第二步：编写 SKILL.md**

```yaml
---
name: my-skill
description: 一句话说清楚这个 Skill 做什么、什么时候触发
---

# 工作流程
1. 第一步做什么
2. 第二步做什么
3. 输出什么格式

# 决策规则
- 什么时候走分支 A
- 什么时候走分支 B
```

**第三步：使用**

```bash
/my-skill <参数>
```

### 8.2 Skill 设计清单

```
□ 我的 Skill 解决什么问题？
□ 这个问题是否重复出现？（是 → 值得做 Skill）
□ 需要 Claude 知道哪些领域知识？
□ 需要什么外部工具？（MCP / Bash / API）
□ 输出格式是什么？（报告 / 代码 / 操作）
□ 是否需要参考文档？（放 references/ 目录）
□ 谁来触发？（自动触发 vs 手动 /name）
```

### 8.3 你可以做的 Skill 示例

| 你的角色 | Skill 思路 | 核心价值 |
|---------|-----------|---------|
| **后端开发** | API 接口审查专家 | 自动检查接口规范、安全性、性能 |
| **前端开发** | 组件迁移助手 | 按标准流程从框架 A 迁移到框架 B |
| **测试工程师** | 用例生成器 | 读需求文档自动生成测试用例 |
| **DevOps** | 部署故障诊断 | 接入 K8s/监控 MCP，自动分析部署失败原因 |
| **DBA** | SQL 审查专家 | 检查慢查询、索引建议、安全风险 |
| **架构师** | 架构评审助手 | 按团队规范审查设计方案 |
| **技术写作** | 文档生成器 | 读代码自动生成 API 文档 / 变更日志 |

### 8.4 进阶：Skill 存放位置与作用域

```
~/.claude/skills/         → 个人 Skill，所有项目可用
.claude/skills/           → 项目 Skill，随代码提交，团队共享
Enterprise managed        → 组织级 Skill，全员可用
```

**推荐策略**：
- 个人效率工具 → `~/.claude/skills/`
- 团队标准流程 → `.claude/skills/`（提交到 Git）
- 公司规范检查 → Enterprise managed

---

## 九、总结

### Skill 的本质

**Skill 不是在教 Claude 新技能，而是在告诉它——面对这个问题，该怎么像专家一样思考。**

它把你的经验、流程、判断标准编码成可复用的"操作手册"，让 Claude 在你的领域内从"聪明的新人"变成"资深专家"。

### 三个关键认知

1. **门槛很低**：一个 Markdown 文件就是一个 Skill，不需要编程
2. **价值很高**：一次编写，团队复用，每次节省的时间都是收益
3. **越用越好**：随着使用中的反馈不断优化，Skill 会越来越精准

### 行动建议

> 想一想，你工作中有哪些**重复出现的、有固定排查/操作流程的问题**？
> 
> 把它写成一个 Skill，就是你给团队最好的贡献。

从今天开始，打造属于你自己的 Skill 库。

---

## 附录

### A. 演示项目结构

```
pms-demo-ai/
├── demo-service-a/    (8081) API Gateway / BFF
├── demo-service-b/    (8082) Task Service
├── demo-service-c/    (8083) Platform Service (用户 & 通知)
├── deploy/skywalking/ SkyWalking 部署配置
├── skills/            Skill 定义
│   └── skywalking-trace-diagnoser/
│       ├── SKILL.md
│       └── references/
│           ├── scenario-playbook.md
│           ├── code-map.md
│           ├── mcp-contract.md
│           └── analysis-template.md
└── docs/              文档
```

### B. 演示场景速查

| 场景 | API | 隐藏问题 | Trace 特征 |
|------|-----|---------|-----------|
| 仪表盘慢 | `GET /api/projects/1/dashboard` | N+1 远程调用 | 15 个串行 Exit span |
| 通知丢失 | `POST /api/projects/1/tasks` (HIGH) | 静默吞错 | 子 span ERROR + 父 span OK |
| 概览超时 | `GET /api/projects/1/overview?includeStats=true` | 级联超时 | 并行 span 一快一慢 |
| 导入失败 | `POST /api/projects/1/tasks/import` | 错误详情丢失 | 部分子 span 404 |
| 重复通知 | `POST /api/projects/1/tasks/1/complete` | 重试风暴 | 3 个同端点 span |

### C. 参考资料

- [Claude Code 官方文档 - Skills](https://docs.anthropic.com/en/docs/claude-code/skills)
- [Agent Skills 开放标准](https://agentskills.io)
- [SkyWalking MCP Server](https://github.com/apache/skywalking-mcp)
- [Model Context Protocol](https://modelcontextprotocol.io)
