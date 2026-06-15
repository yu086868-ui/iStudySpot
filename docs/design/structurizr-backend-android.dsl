workspace "iStudySpot 后端与 Android C4模型" "基于 backend/istudyspot-backend 与 frontend/Android 当前代码整理的 C4 模型。" {
    !docs structurizr
    !adrs structurizr/decisions

    model {
        properties {
            "structurizr.groupSeparator" "/"
        }

        student = person "学生用户" "通过 Android App 浏览自习室、预约座位、管理订单及相关增值服务。"

        deepseek = softwareSystem "DeepSeek API" "供后端 AI 对话、Agent 推理、客服回复与卡牌文案生成调用的外部 LLM API。" {
            tags "External"
        }

        imagegen = softwareSystem "本地图片生成服务" "供后端生成学习卡牌图片时调用的本地 HTTP 服务。" {
            tags "External"
        }

        istudyspot = softwareSystem "iStudySpot" "面向自习室预约与学习场景的系统，包含 AI 辅助等学习增值服务；本模型仅覆盖当前后端与 Android 代码范围。" {
            !docs structurizr
            !adrs structurizr/decisions

            androidApp = container "Android App" "面向学生用户的移动端客户端，使用 Jetpack Compose、Navigation Compose、ViewModel、协程、Retrofit 与 OkHttp 实现。" "Kotlin, Jetpack Compose, MVVM" {
                authAndSession = component "导航与会话引导" "由 MainActivity 与 AppNavigation 组成，负责应用壳层、底部导航、Snackbar、路由图，以及把已保存的访问令牌注入 ApiClient。" "MainActivity.kt, AppNavigation.kt, NavRoutes"
                composeScreens = component "Compose 页面" "承载首页、自习室、座位图、预约、订单、个人中心、AI 对话、Agent、客服、卡牌收藏、待办等功能页面。" "ui/screen/*"
                viewModels = component "ViewModels" "负责页面状态、异步副作用与仓储调用；当前客户端主要使用普通请求-响应接口，并未接入后端提供的 SSE 页面流程。" "viewmodel/*"
                repository = component "MainRepository" "统一暴露客户端用例，并把请求委派给 ApiManager。" "repository/MainRepository.kt"
                apiGateway = component "API 管理与接口契约" "由 ApiManager 与 ApiService 组成，负责 Retrofit 调用、错误归一化与可选 mock 模式。" "infra/network/ApiManager.kt, ApiService.kt"
                networkStack = component "Retrofit 与 OkHttp 网络栈" "由 ApiClient 配置 Retrofit、Bearer 令牌注入、刷新令牌鉴权路径、日志与调试环境下的证书放宽策略。" "infra/network/ApiClient.kt"
                localSettings = component "SharedPreferences 配置存储" "由 ConfigManager 持久化访问令牌、用户标识、昵称与主题模式；当前代码未真正持久化 refresh token。" "utils/ConfigManager.kt"
            }

            backendApi = container "Backend API" "单体 Spring Boot 应用，对外提供认证、自习室、座位、预约、支付、AI、Agent、客服、卡牌、规则、个人资料、违规、成就、统计与待办接口。" "Java 17, Spring Boot 3.1, Spring MVC, MyBatis" {
                authApi = component "认证与请求保护" "由 AuthController、AuthServiceImpl、JwtInterceptor、JwtUtils 与 WebConfig 组成，负责登录、注册、刷新令牌、登出与受保护接口访问。" "controller/AuthController.java, service/impl/AuthServiceImpl.java, interceptor/JwtInterceptor.java, config/WebConfig.java"
                studyRoomApi = component "自习室与座位接口" "由 StudyRoomController、SeatController、StudyRoomServiceImpl 与 SeatServiceImpl 组成，负责自习室列表、详情、座位列表与座位详情。" "controller/StudyRoomController.java, controller/SeatController.java, service/impl/StudyRoomServiceImpl.java, service/impl/SeatServiceImpl.java"
                reservationApi = component "预约、签到与支付接口" "由 OrderController、CheckInController、PaymentController、OrderServiceImpl 与 PaymentServiceImpl 组成，负责预约生命周期、支付、签到、签退、续时与简化支付回调处理。" "controller/OrderController.java, controller/CheckInController.java, controller/PaymentController.java, service/impl/OrderServiceImpl.java, service/impl/PaymentServiceImpl.java"
                userContentApi = component "用户资料、成就、待办、违规、规则、公告与统计接口" "组合用户资料与用户侧内容接口；其中部分模块有数据库支撑，而规则主要来自静态代码/配置，公告与统计当前仍以占位返回为主。" "controller/UserController.java, controller/AchievementController.java, controller/TodoController.java, controller/ViolationRecordController.java, controller/RulesController.java, controller/AnnouncementController.java, controller/StatisticsController.java"
                aiApi = component "AI、Agent、客服与卡牌接口" "由 AIController、AgentChatController、AgentToolController、CustomerServiceController、CardController 及相关服务组成，负责 AI 对话、Agent 只读工具编排、客服对话与学习卡牌生成；AI、Agent 与客服会话历史当前保存在服务内存中。" "controller/AIController.java, controller/AgentChatController.java, controller/AgentToolController.java, controller/CustomerServiceController.java, controller/CardController.java"
                agentPolicyGuard = component "Agent LLM 策略守卫" "读取 ai-rules.json 中的安全、隐私、工具与客服事实规则，并通过 DeepSeek 判定请求是否属于允许的只读信息查询；请求 Agent 执行预约、取消、签到、签退、支付、续时等写操作时直接返回只读说明。" "agent/chat/AgentChatServiceImpl.java, ai/AiRulesRegistry.java, resources/ai-rules.json"
                agentOrchestrator = component "Agent LLM 编排器" "把只读工具目录转换为 LLM tool schema，调用 DeepSeek 完成工具选择与最终回答生成；在 LLM 编排不可用时退回到确定性的只读查询路由。" "agent/chat/AgentChatServiceImpl.java, service/DeepSeekService.java"
                agentToolGateway = component "Agent 只读工具网关" "发布并执行 Agent 的只读工具：自习室列表、详情、座位列表、当前用户预约摘要与预约规则；后端工具白名单作为最终边界，不暴露预约、取消、签到或支付类写工具。" "controller/AgentToolController.java, agent/tool/AgentToolServiceImpl.java, agent/tool/ReservationRulesProvider.java"
                agentSessionContext = component "Agent 短期会话上下文" "在 AgentChatServiceImpl 内以内存 Map 保存短期对话、用户会话标识与当前自习室线索，用于承接后续只读追问。" "agent/chat/AgentChatServiceImpl.java"
                observability = component "请求指标与定时告警" "由 MetricsInterceptor 与 AlertServiceImpl 组成，在内存中统计请求量与延迟，并按阈值执行定时告警检查。" "interceptor/MetricsInterceptor.java, service/impl/AlertServiceImpl.java"
                persistence = component "MyBatis 持久化层" "由 MyBatis Mapper、XML 映射、实体与 SQL 资源组成，支撑用户、自习室、座位、预约、支付、卡牌、待办、违规与成就等数据读写。" "mapper/*, resources/mapper/*, resources/schema/*, resources/db/migration/*"
                aiRulesConfig = component "AI 规则与预约策略配置" "从 `ai-rules.json` 加载提示词规则、预约策略与客服引导信息，供 AI 服务与 Agent 工具层复用。" "resources/ai-rules.json, ai/*, agent/tool/ReservationRulesProvider.java"
                localArtifacts = component "本地卡牌图片存储" "把生成后的卡牌 PNG 文件写入本地目录，并通过 `/api/card/image/**` 对外提供访问。" "service/impl/CardServiceImpl.java, controller/CardController.java"
            }

            mysql = container "MySQL 数据库" "存储用户、自习室、座位、预约、支付、卡牌、待办、违规、成就等业务数据。" "MySQL" {
                tags "Database"
            }

            uploads = container "卡牌图片存储目录" "用于保存生成卡牌图片的本地文件系统目录。" "本地文件系统"
        }

        student -> androidApp "使用移动端客户端" "触控界面"
        androidApp -> backendApi "调用后端接口完成业务流程" "HTTPS/JSON"
        backendApi -> mysql "读写业务数据" "JDBC/MyBatis"
        backendApi -> uploads "读写生成后的卡牌图片" "文件系统读写"
        backendApi -> deepseek "调用 AI 对话、Agent 推理、客服回复与卡牌文案生成能力" "HTTPS/JSON"
        backendApi -> imagegen "调用本地图片生成接口生成卡牌图片" "HTTP/JSON"

        authAndSession -> composeScreens "承载并导航到各功能页面" "Navigation Compose"
        authAndSession -> localSettings "读取已持久化的访问令牌与主题模式" "SharedPreferences"
        authAndSession -> networkStack "把保存的访问令牌引导注入" "进程内 Kotlin 调用"
        composeScreens -> viewModels "派发页面动作并观察状态" "Compose 状态与 StateFlow"
        viewModels -> repository "调用应用用例" "Kotlin 协程调用"
        viewModels -> localSettings "读写登录、会话与个人偏好配置" "SharedPreferences"
        repository -> apiGateway "委派远程业务请求" "Kotlin 协程调用"
        apiGateway -> networkStack "通过网络层执行请求" "Retrofit"
        networkStack -> backendApi "调用后端 REST 接口" "HTTPS/JSON"

        authApi -> persistence "读写用户与令牌相关数据" "MyBatis"
        studyRoomApi -> persistence "读取自习室、座位与预约上下文数据" "MyBatis"
        reservationApi -> persistence "读写预约、座位与支付数据" "MyBatis"
        userContentApi -> persistence "读写资料、成就、待办与违规记录" "MyBatis"
        userContentApi -> aiRulesConfig "读取预约与使用规则配置" "Classpath JSON 配置"
        aiApi -> persistence "读写卡牌记录与 Agent 暴露的业务数据" "MyBatis"
        aiApi -> aiRulesConfig "加载 AI 人设、客服规则与预约策略配置" "Classpath JSON 配置"
        reservationApi -> authApi "依赖其建立的认证请求上下文" "请求属性 / interceptor 链"
        userContentApi -> authApi "依赖其建立的认证请求上下文" "请求属性 / interceptor 链"
        aiApi -> authApi "依赖其建立的认证请求上下文" "请求属性 / interceptor 链"
        aiApi -> agentPolicyGuard "Agent 对话入口先执行基于规则的 LLM 只读策略判定" "Java 方法调用"
        agentPolicyGuard -> aiRulesConfig "加载 Agent 安全、隐私、工具与客服事实规则" "Classpath JSON 配置"
        agentPolicyGuard -> deepseek "请求 LLM 按规则判定只读查询与写操作意图" "HTTPS/JSON"
        aiApi -> agentOrchestrator "把允许的 Agent 请求交给 LLM 工具编排流程" "Java 方法调用"
        agentOrchestrator -> deepseek "请求工具调用规划与最终回答生成" "HTTPS/JSON"
        agentOrchestrator -> agentToolGateway "只执行 LLM 选中的后端白名单只读工具" "Java 方法调用"
        agentOrchestrator -> agentSessionContext "读取并更新短期会话与自习室上下文" "内存 Map"
        agentToolGateway -> authApi "使用认证链路写入的用户上下文执行用户相关只读查询" "请求属性 / interceptor 链"
        agentToolGateway -> studyRoomApi "复用自习室与座位服务读取房间、详情和座位数据" "Java 服务调用"
        agentToolGateway -> reservationApi "复用订单服务读取当前用户预约摘要，并做引用化与必要脱敏" "Java 服务调用"
        agentToolGateway -> aiRulesConfig "读取共享预约规则并作为只读工具结果返回" "Classpath JSON 配置"
        observability -> authApi "围绕请求链路收集认证与非认证流量指标" "Spring MVC interceptor 链"
        persistence -> mysql "把实体映射到关系型表结构" "SQL"
        aiApi -> deepseek "调用 LLM 补全与流式对话能力" "HTTPS/JSON"
        aiApi -> imagegen "调用图片生成能力" "HTTP/JSON"
        aiApi -> localArtifacts "写入生成后的卡牌图片" "文件系统读写"
        localArtifacts -> uploads "把文件保存到目录中" "文件系统路径"
    }

    views {
        systemContext istudyspot "system-context" "C1：当前 iStudySpot 产品中后端与 Android 相关范围的系统上下文图，聚焦学生用户与系统本身的业务关系。" {
            include student
            include istudyspot
            autolayout lr
        }

        container istudyspot "container" "C2：当前后端与 Android 范围内的容器图，展示后端对外部 AI 与图片生成服务的容器级依赖。" {
            include student
            include deepseek
            include imagegen
            include androidApp
            include backendApi
            include mysql
            include uploads
            autolayout lr
        }

        component backendApi "backend-components" "C3：当前 Spring Boot 单体后端中的主要组件分工图。" {
            include authApi
            include studyRoomApi
            include reservationApi
            include userContentApi
            include aiApi
            include agentPolicyGuard
            include agentOrchestrator
            include agentToolGateway
            include agentSessionContext
            include observability
            include persistence
            include aiRulesConfig
            include localArtifacts
            include deepseek
            include imagegen
            include mysql
            include uploads
            autolayout lr
        }

        component androidApp "android-components" "C3：当前 Android 客户端基于 Compose 与 MVVM 的主要组件分工图。" {
            include authAndSession
            include composeScreens
            include viewModels
            include repository
            include apiGateway
            include networkStack
            include localSettings
            include backendApi
            autolayout lr
        }

        styles {
            element "Person" {
                background "#08427b"
                color "#ffffff"
                shape person
            }
            element "Software System" {
                background "#1168bd"
                color "#ffffff"
            }
            element "Container" {
                background "#438dd5"
                color "#ffffff"
            }
            element "Component" {
                background "#85bbf0"
                color "#000000"
            }
            element "Database" {
                shape cylinder
            }
            element "External" {
                background "#999999"
                color "#ffffff"
            }
        }

    }

    configuration {
        scope softwaresystem
    }
}
