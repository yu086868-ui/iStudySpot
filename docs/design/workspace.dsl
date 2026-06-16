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
                appShell = component "应用壳层与路由编排" "负责启动恢复、全局容器、页面装配、跨页面跳转，以及把本地会话状态注入网络层。" "Navigation Compose, Scaffold"
                authFeature = component "认证与登录功能域" "负责登录、注册、基础校验，以及建立客户端本地会话状态并切换访问态。" "Compose, ViewModel, StateFlow"
                homeFeature = component "首页与通知功能域" "负责首页统计摘要、快捷入口、公告通知与入口汇聚，并保障通知内容在不同数据状态下的稳定展示。" "Compose, ViewModel, StateFlow"
                studyRoomFeature = component "自习室、场馆导览与座位图功能域" "负责自习室检索、场馆导览、座位布局加载、座位状态展示与可预约座位选择。" "Compose, ViewModel, StateFlow"
                reservationFeature = component "预约、订单与签到签退功能域" "负责时间选择、下单、支付、取消、续时、签到与签退等预约生命周期操作。" "Compose, ViewModel, StateFlow"
                profileFeature = component "个人资料、偏好设置与更多菜单功能域" "负责个人资料展示与编辑、主题设置、功能入口整合与退出登录。" "Compose, ViewModel, StateFlow"
                growthFeature = component "规则、学习记录、违规、成就与卡牌功能域" "负责规则查询、学习统计、待办入口、违规记录、成就展示与卡牌收藏浏览。" "Compose, ViewModel, StateFlow"
                aiChatFeature = component "AI 咨询功能域" "负责角色选择、消息列表维护、会话状态管理与咨询回复呈现，支持多种运行配置下的统一交互体验。" "Compose, ViewModel, StateFlow"
                agentFeature = component "Agent 助手功能域" "负责工具目录加载、多会话切换、结果折叠展示、页面跳转动作映射，以及会话置顶与删除管理。" "Compose, ViewModel, SharedPreferences"
                customerServiceFeature = component "在线客服功能域" "负责欢迎语、推荐问题、消息发送、会话渲染，并维护客服会话的前端状态。" "Compose, ViewModel, StateFlow"
                composeScreens = component "Compose 页面基座" "承载跨功能域的界面实现与 UI 复用，覆盖首页、自习室、场馆导览、座位图、预约、订单、个人中心、AI、Agent、客服、卡牌收藏与待办等界面层。" "Jetpack Compose"
                viewModels = component "ViewModel 状态编排层" "负责页面状态、异步副作用、会话编排与仓储调用，承接客户端各功能域的交互状态流转。" "ViewModel, StateFlow, Coroutines"
                repository = component "MainRepository 远程用例门面" "统一封装认证、自习室、场馆导览、预约、订单、用户、规则、公告、AI、Agent、客服、卡牌与学习记录等用例，并把请求委派给接口管理层。" "Repository Pattern"
                apiGateway = component "API 管理与接口契约" "负责 Retrofit 调用、错误归一化、接口参数适配与多环境访问策略管理。" "ApiManager, Retrofit Service"
                networkStack = component "Retrofit 与 OkHttp 网络栈" "负责 HTTP 客户端配置、访问令牌注入、刷新令牌鉴权路径、日志与调试环境下的证书放宽策略。" "Retrofit, OkHttp, Authenticator"
                localSettings = component "本地配置与轻量持久化" "负责访问令牌、用户标识、昵称、主题模式与 Agent 历史会话摘要等轻量数据的本地保存，支撑客户端会话恢复与个性化配置。" "SharedPreferences"
            }

            backendApi = container "Backend API" "单体 Spring Boot 应用，对外提供认证、自习室、座位、预约、支付、AI、Agent、客服、卡牌、规则、个人资料、违规、成就、统计与待办接口。" "Java 17, Spring Boot 3.1, Spring MVC, MyBatis" {
                authApi = component "认证与请求保护" "负责登录、注册、刷新令牌、登出、JWT 校验与受保护接口访问控制。" "Spring MVC, Service Layer, JWT"
                studyRoomApi = component "自习室与座位接口" "负责自习室列表、详情、场馆导览、座位列表、座位布局与座位详情等学习空间查询能力。" "Spring MVC, Service Layer, MyBatis"
                reservationApi = component "预约、签到与支付接口" "负责预约生命周期、支付、签到、签退、续时与支付结果处理。" "Spring MVC, Service Layer, MyBatis"
                userContentApi = component "用户资料、成就、待办、违规、规则、公告与统计接口" "负责用户资料与用户侧内容服务编排，覆盖规则、公告、统计、违规、待办与成长相关内容。" "Spring MVC, Service Layer, MyBatis"
                aiApi = component "AI、Agent、客服与卡牌接口" "负责 AI 对话、Agent 只读工具编排、客服对话与学习卡牌生成，并维护相关会话上下文与交互记录。" "Spring MVC, Service Layer, LLM Integration"
                agentPolicyGuard = component "Agent LLM 策略守卫" "负责加载安全、隐私、工具与客服事实规则，并判定请求是否属于允许的只读信息查询；对写操作意图统一返回只读边界说明。" "Rule Engine, LLM Classification"
                agentOrchestrator = component "Agent LLM 编排器" "负责把只读工具目录转换为 LLM tool schema，完成工具选择、结果组织与最终回答生成。" "LLM Orchestration, Tool Calling"
                agentToolGateway = component "Agent 只读工具网关" "负责发布并执行 Agent 可访问的只读工具，并以工具白名单作为能力边界控制。" "Tool Gateway, Service Composition"
                agentSessionContext = component "Agent 短期会话上下文" "负责维护短期对话、用户会话标识与当前自习室线索，用于承接后续只读追问。" "In-memory Session Context"
                observability = component "请求指标与定时告警" "负责请求量、延迟等运行指标采集，以及按阈值执行定时告警检查。" "Interceptor, Metrics, Scheduled Tasks"
                persistence = component "MyBatis 持久化层" "负责用户、自习室、场馆导览、座位、预约、支付、卡牌、待办、违规与成就等数据读写。" "MyBatis, SQL, Flyway, MySQL"
                aiRulesConfig = component "AI 规则与预约策略配置" "负责加载提示词规则、预约策略与客服引导信息，供 AI 服务与 Agent 工具层复用。" "Classpath JSON Configuration"
                localArtifacts = component "本地卡牌图片存储" "负责写入生成后的卡牌图片文件，并对外提供图片访问路径。" "Local File Storage"
            }

            mysql = container "MySQL 数据库" "存储用户、自习室、场馆导览、座位、预约、支付、卡牌、待办、违规、成就等业务数据。" "MySQL" {
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

        appShell -> authFeature "装配登录、注册与会话切换页面" "Navigation Compose"
        appShell -> homeFeature "装配首页与通知入口" "Navigation Compose"
        appShell -> studyRoomFeature "装配自习室、场馆导览与座位图流程" "Navigation Compose"
        appShell -> reservationFeature "装配预约、订单与签到签退流程" "Navigation Compose"
        appShell -> profileFeature "装配个人中心、偏好设置与更多菜单" "Navigation Compose"
        appShell -> growthFeature "装配规则、学习记录、违规、成就与卡牌页面" "Navigation Compose"
        appShell -> aiChatFeature "装配角色选择与 AI 咨询流程" "Navigation Compose"
        appShell -> agentFeature "装配 Agent 助手多会话页面" "Navigation Compose"
        appShell -> customerServiceFeature "装配在线客服页面" "Navigation Compose"
        appShell -> localSettings "读取已持久化的访问令牌与主题模式" "SharedPreferences"
        appShell -> networkStack "把保存的访问令牌引导注入" "进程内 Kotlin 调用"
        authFeature -> composeScreens "使用登录与注册界面基座" "Compose UI"
        homeFeature -> composeScreens "使用首页与通知界面基座" "Compose UI"
        studyRoomFeature -> composeScreens "使用自习室、导览与座位图界面基座" "Compose UI"
        reservationFeature -> composeScreens "使用预约与订单界面基座" "Compose UI"
        profileFeature -> composeScreens "使用个人资料与偏好设置界面基座" "Compose UI"
        growthFeature -> composeScreens "使用规则、记录、成就与卡牌界面基座" "Compose UI"
        aiChatFeature -> composeScreens "使用 AI 咨询界面基座" "Compose UI"
        agentFeature -> composeScreens "使用 Agent 助手界面基座" "Compose UI"
        customerServiceFeature -> composeScreens "使用客服界面基座" "Compose UI"
        authFeature -> viewModels "通过 AuthViewModel 执行认证状态编排" "StateFlow / 协程"
        homeFeature -> viewModels "通过 Home/Notification ViewModel 编排首页与通知状态" "StateFlow / 协程"
        studyRoomFeature -> viewModels "通过 StudyRoom/Guide ViewModel 编排列表、导览与座位状态" "StateFlow / 协程"
        reservationFeature -> viewModels "通过 Booking/Order ViewModel 编排预约生命周期状态" "StateFlow / 协程"
        profileFeature -> viewModels "通过 Profile 相关 ViewModel 编排资料与偏好状态" "StateFlow / 协程"
        growthFeature -> viewModels "通过 Rules/StudyRecord/Todo/Card 等 ViewModel 编排内容状态" "StateFlow / 协程"
        aiChatFeature -> viewModels "通过 AiChatViewModel 编排角色与消息状态" "StateFlow / 协程"
        agentFeature -> viewModels "通过 AgentViewModel 编排工具、消息与历史会话状态" "StateFlow / 协程"
        customerServiceFeature -> viewModels "通过 CustomerServiceViewModel 编排客服消息状态" "StateFlow / 协程"
        viewModels -> repository "调用应用用例" "Kotlin 协程调用"
        viewModels -> localSettings "读写登录、会话与个人偏好配置" "SharedPreferences"
        repository -> apiGateway "委派远程业务请求" "Kotlin 协程调用"
        apiGateway -> networkStack "通过网络层执行请求" "Retrofit"
        networkStack -> backendApi "调用后端 REST 接口" "HTTPS/JSON"

        authApi -> persistence "读写用户数据并更新最后登录时间等认证相关信息" "MyBatis"
        studyRoomApi -> persistence "读取自习室、场馆导览、座位与预约上下文数据" "MyBatis"
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
            include appShell
            include authFeature
            include homeFeature
            include studyRoomFeature
            include reservationFeature
            include profileFeature
            include growthFeature
            include aiChatFeature
            include agentFeature
            include customerServiceFeature
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
