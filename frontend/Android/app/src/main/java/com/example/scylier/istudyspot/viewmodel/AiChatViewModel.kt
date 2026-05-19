package com.example.scylier.istudyspot.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.BuildConfig
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.ai.AiCharacter
import com.example.scylier.istudyspot.models.ai.AiChatResponse
import com.example.scylier.istudyspot.models.ai.AiMessage
import com.example.scylier.istudyspot.models.ai.MessageType
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AiChatViewModel(
    private val repository: MainRepository = MainRepository(),
    private val useMock: Boolean = BuildConfig.USE_MOCK
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiMessage>>(emptyList())
    val messages: StateFlow<List<AiMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _characters = MutableStateFlow<List<AiCharacter>>(defaultCharacters)
    val characters: StateFlow<List<AiCharacter>> = _characters

    private val _selectedCharacter = MutableStateFlow<AiCharacter?>(null)
    val selectedCharacter: StateFlow<AiCharacter?> = _selectedCharacter

    private var sessionId: String? = null

    fun selectCharacter(character: AiCharacter) {
        _selectedCharacter.value = character
    }

    fun sendMessage(content: String) {
        val userMessage = AiMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            type = MessageType.USER
        )
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = if (useMock) {
                    getMockResponse(content)
                } else {
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
                        _messages.value = _messages.value + aiMessage
                    }
                    is ApiResponse.Error -> {
                        val errorMessage = AiMessage(
                            id = UUID.randomUUID().toString(),
                            content = "抱歉，我暂时无法回答您的问题，请稍后再试。",
                            type = MessageType.AI
                        )
                        _messages.value = _messages.value + errorMessage
                    }
                }
            } catch (e: Exception) {
                val errorMessage = AiMessage(
                    id = UUID.randomUUID().toString(),
                    content = "网络连接异常，请检查网络后重试。",
                    type = MessageType.AI
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callApi(content: String): ApiResponse<AiChatResponse> {
        return try {
            repository.sendAiMessage(content, sessionId, _selectedCharacter.value?.id)
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "未知错误")
        }
    }

    private suspend fun getMockResponse(content: String): ApiResponse<AiChatResponse> {
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

周一至周日：07:00 - 23:00

各时段说明：
- 早场：07:00 - 12:00
- 下午场：12:00 - 18:00
- 晚场：18:00 - 23:00

温馨提示：
- 节假日正常开放
- 考试周期间会延长开放时间至24:00
- 建议提前预约，尤其是晚场时段较为紧张"""
            }
            content.contains("取消") -> {
                """取消预约的规则如下：

1. 在预约开始前30分钟可以免费取消
2. 预约开始前30分钟内取消将记录一次违规
3. 未签到也将记录一次违规
4. 累计3次违规将被禁止预约7天

取消方式：
- 进入"我的预约"页面
- 找到对应订单，点击"取消预约"按钮
- 确认取消即可"""
            }
            content.contains("价格") || content.contains("费用") || content.contains("多少钱") -> {
                """座位价格信息：

普通座位：10元/小时
VIP座位：15元/小时

优惠信息：
- 充值满100送20
- 学生认证用户享9折优惠
- 长期包月更优惠

支付方式：
- 微信支付
- 支付宝
- 余额支付"""
            }
            content.contains("规则") -> {
                """自习室使用规则：

1. 预约规则：每人每天最多预约1个座位
2. 签到规则：预约后30分钟内需签到
3. 暂离规则：暂离不超过30分钟，每天最多3次
4. 违规处理：累计3次违规禁止预约7天
5. 文明使用：保持安静，不得占座不用

详细规则请在"更多"页面查看。"""
            }
            else -> {
                val character = _selectedCharacter.value
                if (character != null) {
                    "${character.persona}，请问有什么可以帮您的？"
                } else {
                    """感谢您的提问！我是iStudySpot的AI咨询助手，可以帮您解答以下问题：

- 如何预约座位
- 签到/签退流程
- 自习室开放时间
- 取消预约规则
- 座位价格和支付
- 自习室使用规则

请告诉我您想了解哪方面的信息？"""
                }
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
        _messages.value = emptyList()
        sessionId = null
    }

    companion object {
        val defaultCharacters = listOf(
            AiCharacter(
                id = "xuemaomao",
                name = "学霸猫",
                persona = "我是学霸猫，擅长学习规划和方法论",
                speakingStyle = "轻松活泼",
                avatarColor = Color(0xFF8B5CF6)
            ),
            AiCharacter(
                id = "wenrouxuejie",
                name = "温柔学姐",
                persona = "我是温柔学姐，耐心解答你的每一个问题",
                speakingStyle = "温柔细致",
                avatarColor = Color(0xFFEC4899)
            ),
            AiCharacter(
                id = "yanlidaooshi",
                name = "严厉导师",
                persona = "我是严厉导师，帮你养成高效学习习惯",
                speakingStyle = "简洁有力",
                avatarColor = Color(0xFF3B82F6)
            )
        )
    }
}
