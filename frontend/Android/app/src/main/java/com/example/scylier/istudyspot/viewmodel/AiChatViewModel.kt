package com.example.scylier.istudyspot.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.infra.network.ApiService
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.ai.AiChatRequest
import com.example.scylier.istudyspot.models.ai.AiChatResponse
import com.example.scylier.istudyspot.models.ai.AiMessage
import com.example.scylier.istudyspot.models.ai.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AiChatViewModel : ViewModel() {

    private val _messages = mutableStateListOf<AiMessage>()
    val messages: List<AiMessage> get() = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var sessionId: String? = null
    private val apiService = ApiClient.createService(ApiService::class.java)

    // Mock模式开关 - 设置为true使用mock数据
    private val useMock = true

    fun sendMessage(content: String) {
        // 添加用户消息
        val userMessage = AiMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            type = MessageType.USER
        )
        _messages.add(userMessage)

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = if (useMock) {
                    // 使用Mock数据
                    getMockResponse(content)
                } else {
                    // 使用真实API
                    callApi(content)
                }

                when (response) {
                    is ApiResponse.Success -> {
                        sessionId = response.data.sessionId
                        val aiMessage = AiMessage(
                            id = UUID.randomUUID().toString(),
                            content = response.data.reply,
                            type = MessageType.AI
                        )
                        _messages.add(aiMessage)
                    }
                    is ApiResponse.Error -> {
                        val errorMessage = AiMessage(
                            id = UUID.randomUUID().toString(),
                            content = "抱歉，我暂时无法回答您的问题，请稍后再试。",
                            type = MessageType.AI
                        )
                        _messages.add(errorMessage)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = AiMessage(
                    id = UUID.randomUUID().toString(),
                    content = "网络连接异常，请检查网络后重试。",
                    type = MessageType.AI
                )
                _messages.add(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callApi(content: String): ApiResponse<AiChatResponse> {
        return try {
            val request = AiChatRequest(
                message = content,
                sessionId = sessionId
            )
            val response = apiService.sendAiMessage(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null) {
                    ApiResponse.Success(body.code, body.message, body.data)
                } else {
                    ApiResponse.Error(body.code, body.message)
                }
            } else {
                ApiResponse.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "未知错误")
        }
    }

    private suspend fun getMockResponse(content: String): ApiResponse<AiChatResponse> {
        // 模拟网络延迟
        kotlinx.coroutines.delay(800)

        val reply = when {
            content.contains("预约") || content.contains("预订") -> {
                """预约座位的流程如下：

1. 打开APP，点击首页的"预约座位"
2. 选择您想要去的自习室
3. 在座位图上选择空闲的座位（绿色表示可预约）
4. 选择预约的开始时间和结束时间
5. 确认订单信息并完成支付
6. 预约成功后，在预约时间到达自习室签到即可

温馨提示：
- 请提前15分钟到达并签到
- 如需取消预约，请在开始前30分钟操作
- 爽约3次将被限制预约功能"""
            }
            content.contains("签到") -> {
                """签到流程非常简单：

1. 到达自习室后，打开APP
2. 进入"我的预约"页面
3. 找到当前预约的订单，点击"签到"按钮
4. 系统会自动定位，确认您在自习室范围内即可完成签到

注意事项：
- 请在预约开始时间前后15分钟内签到
- 超过时间未签到将视为爽约
- 签到后请保持手机蓝牙开启，以便系统检测"""
            }
            content.contains("时间") || content.contains("开放") -> {
                """自习室开放时间：

📅 周一至周日：07:00 - 23:00

⏰ 各时段说明：
- 早场：07:00 - 12:00
- 下午场：12:00 - 18:00
- 晚场：18:00 - 23:00

💡 温馨提示：
- 节假日正常开放
- 考试周期间会延长开放时间至24:00
- 建议提前预约，尤其是晚场时段较为紧张"""
            }
            content.contains("取消") || content.contains("退订") -> {
                """取消预约规则：

✅ 免费取消：
- 预约开始前30分钟以上取消，全额退款

⚠️ 限制取消：
- 预约开始前15-30分钟取消，收取20%手续费
- 预约开始前15分钟内，不可取消

❌ 爽约处理：
- 未按时签到视为爽约
- 爽约3次将暂停预约权限7天
- 爽约5次将暂停预约权限30天

退款将在1-3个工作日内原路返回。"""
            }
            content.contains("价格") || content.contains("费用") || content.contains("多少钱") -> {
                """座位价格说明：

💺 普通座位：
- 2元/小时
- 包时段优惠：早场8元，下午场10元，晚场8元

🛋️ 舒适座位（带插座）：
- 3元/小时
- 包时段优惠：早场12元，下午场15元，晚场12元

📚 研讨室（4-6人）：
- 15元/小时
- 需提前1天预约

💳 支付方式：
- 微信支付
- 支付宝
- 校园卡"""
            }
            content.contains("规则") || content.contains("规定") -> {
                """自习室使用规则：

📱 预约规则：
- 每人每天最多预约2个时段
- 单次预约最长8小时
- 需提前15分钟签到

🔇 行为规范：
- 保持安静，禁止大声喧哗
- 手机请调至静音或震动
- 禁止在座位上用餐
- 离座超过30分钟视为放弃座位

⚠️ 违规处理：
- 首次违规：警告
- 二次违规：暂停预约3天
- 三次违规：暂停预约7天
- 严重违规：永久封禁"""
            }
            else -> {
                """感谢您的提问！我是iStudySpot的AI咨询助手，可以帮您解答以下问题：

• 如何预约座位
• 签到/签退流程
• 自习室开放时间
• 取消预约规则
• 座位价格和支付
• 自习室使用规则

请告诉我您想了解哪方面的信息？"""
            }
        }

        return ApiResponse.Success(
            code = 200,
            message = "success",
            data = AiChatResponse(
                reply = reply,
                sessionId = sessionId ?: UUID.randomUUID().toString()
            )
        )
    }

    fun clearMessages() {
        _messages.clear()
        sessionId = null
    }
}
