# yiz xg Mod — CLAUDE.md

NeoForge 1.21.1 **功能模组**，MODID=`yizxgmod`，依赖前置库 `yizmodqzk`（YizMod QZK）。

## 构建命令

```bash
./gradlew runClient    # 启动游戏（需先 publish 前置）
./gradlew build        # 构建 JAR
```

## 依赖关系

- 前置库路径：`D:\ZM\yiz1.21.1`
- 本地仓库：`D:\ZM\yiz1.21.1\repo`
- 构建前需先在前置库执行 `./gradlew publish`，再在本项目运行

## 模组功能

- 星空体体系（StarBodyEffect）：星之空物品激活 → 下界之星升级（最高 10 层）
- 12 种注册表效果：伤害增幅/减免、回击、复活、投射物反射/免疫、碰撞免疫、击退霸体、飞行优化/权限
- PlayerDataAPI 持久化星光层数 + 自动网络同步
- 天赋面板三段式显示（A/B/C 格式）

## AI 快速入口

- **前置库 API 知识库**（结构化 JSON，AI 直接读取）：`D:\ZM\yizqzk-docs\docs\llm\knowledge.json`
- **前置库文档网站**：`http://localhost:8080`
- **更新文档**：在会话中调用 `/YIZwikl` Skill
