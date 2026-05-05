## 1. 目标

&#x20;

本项目采用渐进式迁移策略，将 Fragment + XML UI 逐步迁移至 Jetpack Compose，实现：

- UI 层现代化（Compose）
- 业务逻辑不变
- 迁移过程低风险
- 支持逐页面替换

***

## 2. 基本原则

### 2.1 不允许修改的内容

- API 请求层（ApiManager / Repository）
- 数据模型（models）
- 错误处理逻辑（ErrorHandler）
- ViewModel（如存在）

***

### 2.2 UI 层唯一迁移目标

- XML Layout → Compose UI
- View 事件绑定 → Compose callback
- findViewById → state-driven UI

***

### 2.3 Fragment 职责调整

迁移后 Fragment 仅作为：

- ComposeView 容器
- Navigation 承载
- 生命周期绑定

***

## 3. 迁移标准模式（必须遵守）

### 3.1 Fragment 模板

所有 Fragment 迁移后必须符合以下结构：

```
class XxxFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                XxxScreen(
                    onAction = { ... }
                )
            }
        }
    }
}
```

***

### 3.2 Compose UI 规范

必须遵守：

#### UI 结构

- &#x20;使用 Column / Row / Box&#x20;
- &#x20;禁止 RelativeLayout / LinearLayout&#x20;
- &#x20;使用 Spacer 代替 margin&#x20;

#### 状态管理

- &#x20;输入框使用 remember { mutableStateOf }&#x20;
- &#x20;UI 必须是 state-driven&#x20;

#### 组件规范

- &#x20;Text / Button / OutlinedTextField / Icon&#x20;
- &#x20;使用 Material3 风格&#x20;

***

### 3.3 交互迁移规则

XML/View

Compose

setOnClickListener

onClick lambda

EditText

OutlinedTextField

TextView\.setText

state → Text

findViewById

state hoisting

***

## 4. 网络与业务逻辑规则

- &#x20;API 调用必须保留原有实现&#x20;
- &#x20;coroutine / suspend function 不得修改&#x20;
- &#x20;UI 通过 callback 触发业务逻辑&#x20;

示例：

```
onLogin = { username, password ->
    login(username, password)
}
```

***

## 5. 迁移流程（强制执行）

### Step 1：Fragment 转 Compose 容器

- &#x20;删除 XML inflate&#x20;
- &#x20;引入 ComposeView&#x20;

### Step 2：XML → Compose 重写

- &#x20;逐块迁移 UI&#x20;
- &#x20;保持功能一致&#x20;

### Step 3：接入状态系统

- &#x20;将 UI 数据 state 化&#x20;

### Step 4：清理 View 代码

- &#x20;移除 findViewById&#x20;
- &#x20;删除旧 XML（确认无引用后）&#x20;

***

## 6. 输出要求（用于 AI / agent）

当执行迁移任务时，必须输出：

1. &#x20;修改后的 Fragment（Compose 化）&#x20;
2. &#x20;完整 Compose UI 实现&#x20;
3. &#x20;简短迁移说明（替换点 + 状态设计）&#x20;

***

## 7. 非功能要求

- &#x20;UI 必须接近生产级别，不允许 demo 风格&#x20;
- &#x20;避免过度嵌套&#x20;
- &#x20;保持代码可复用性&#x20;
- &#x20;优先拆分组件（Screen / Component）

