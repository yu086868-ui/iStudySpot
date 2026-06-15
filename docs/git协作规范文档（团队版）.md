# Git 协作规范文档

## 一、分支结构设计

本项目采用三层分支模型：

```
main（主分支）
└── develop（开发集成分支）
    ├── feature/姓名-功能-doc
    ├── feature/姓名-功能-doc
    └── feature/姓名-功能-doc
```

### 分支说明

- **main 分支**
  - 用途：稳定版本 / 发布版本
  - 特点：始终可运行、无明显 bug
  - 权限：仅组长或负责人可合并

- **develop 分支**
  - 用途：开发集成分支
  - 特点：所有功能最终汇总到此分支
  - 来源：所有 feature 分支合并至此

- **feature 分支**
  - 命名规范：
    ```
    feature/XXX（姓名）-XXX（功能）-doc
    ```
  - 示例：
    ```
    feature/zhangsan-login-doc
    feature/lisi-ui-design-doc
    ```
  - 用途：个人功能开发
  - 生命周期：开发 → 提交 PR → 合并 → 删除

---

## 二、开发流程规范

### 1. 创建开发分支

必须从 develop 分支拉取：

```bash
git checkout develop
git pull
git checkout -b feature/yourname-function-doc
```

---

### 2. 开发与提交

```bash
git add .
git commit -m "feat: 完成XXX功能"
```

提交规范建议：

- feat: 新功能
- fix: 修复问题
- docs: 文档修改
- refactor: 重构

---

### 3. 推送分支

```bash
git push origin feature/yourname-function-doc
```

---

### 4. 发起 Pull Request（PR）

必须遵守：

- base 分支：**develop（严禁选 main）**
- compare 分支：你的 feature 分支

---

### 5. 合并规则

- 由组长或负责人审核后合并
- 禁止自行合并 PR
- 合并方式建议使用：
  - Squash merge（保持历史清晰）

---

### 6. 删除分支

PR 合并后删除 feature 分支：

```bash
git branch -d feature/xxx
```

---

## 三、冲突处理规范

### 1. 冲突产生原因

- 分支长期未同步
- 多人修改同一文件
- 配置文件不统一（如 .gitignore）

---

### 2. 标准处理流程

```bash
git checkout feature/xxx
git pull origin develop
```

解决冲突文件：

```
<<<<<<< HEAD
你的代码
=======
别人的代码
>>>>>>> develop
```

手动修改后：

```bash
git add .
git commit -m "fix: resolve conflict"
git push
```

---

## 四、强制规则（必须遵守）

### ❌ 禁止行为

- 禁止直接向 main 提交代码
- 禁止直接向 develop 提交代码
- 禁止使用 `git push --force`（共享分支）
- 禁止未 pull 最新代码直接开发

---

### ✅ 必须行为

- 每次开发前：
  ```bash
  git checkout develop
  git pull
  ```

- 所有功能必须通过 PR 合并

- PR 必须指向 develop 分支

---

## 五、分支保护建议（GitHub 设置）

### main 分支

- 必须通过 PR 合并
- 至少 1 人 review
- 禁止直接 push

### develop 分支

- 必须通过 PR 合并
- 可设置 review（建议）

---

## 六、团队协作建议

- 每人负责独立功能，减少文件冲突
- 避免多人同时修改同一文件
- 小步提交（避免一次提交大量文件）
- 每天同步一次 develop 分支

---

## 七、典型错误案例（必须避免）

### 错误 1：PR 提交到 main

原因：未修改 base 分支

解决：创建 PR 时确认：

```
base: develop
compare: feature/xxx
```

---

### 错误 2：.gitignore 混乱

原因：不同分支不一致

解决：统一在 develop 中维护

---

### 错误 3：冲突异常复杂

原因：

- 分支长期未同步
- 强制 push

解决：

- 高频同步
- 禁止 force push

---

## 八、总结

本规范核心目标：

- 保证代码稳定（main）
- 保证开发有序（develop）
- 保证个人隔离（feature）

严格执行后，可以显著减少：

- 冲突问题
- 误操作问题
- 合并混乱问题

