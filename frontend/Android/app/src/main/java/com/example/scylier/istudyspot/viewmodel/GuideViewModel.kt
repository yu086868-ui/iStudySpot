package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Facility(
    val name: String,
    val description: String
)

data class GuideUiState(
    val facilities: List<Facility> = emptyList(),
    val location: String = "",
    val openingHours: String = "",
    val contact: String = "",
    val isLoading: Boolean = true
)

class GuideViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {

    private val _state = MutableStateFlow(GuideUiState())
    val state: StateFlow<GuideUiState> = _state

    private fun mockState() = GuideUiState(
        facilities = listOf(
            Facility("静音区", "绝对安静的学习区域，禁止交谈，适合深度学习和阅读"),
            Facility("讨论区", "允许低声讨论的区域，适合小组学习和交流"),
            Facility("休息区", "提供沙发和茶水，可以休息放松"),
            Facility("打印区", "提供自助打印、复印服务"),
            Facility("储物柜", "提供临时储物柜存放个人物品"),
            Facility("饮水机", "免费提供冷热饮用水"),
            Facility("WiFi覆盖", "全馆高速WiFi覆盖，支持在线学习"),
            Facility("空调系统", "中央空调，四季恒温舒适")
        ),
        location = "XX市XX区XX路XX号XX大厦X层",
        openingHours = "周一至周日 08:00 - 23:00（节假日照常营业）",
        contact = "电话：400-XXX-XXXX\n邮箱：contact@istudyspot.com",
        isLoading = false
    )

    fun loadGuideInfo() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                when (val response = repository.getStudyRooms()) {
                    is ApiResponse.Success -> {
                        val rooms = response.data.list
                        if (rooms.isNotEmpty()) {
                            val firstRoom = rooms.first()
                            val facilities = mutableListOf<Facility>()
                            facilities.add(Facility("静音区", "绝对安静的学习区域，禁止交谈，适合深度学习和阅读"))
                            facilities.add(Facility("讨论区", "允许低声讨论的区域，适合小组学习和交流"))
                            facilities.add(Facility("休息区", "提供沙发和茶水，可以休息放松"))
                            facilities.add(Facility("打印区", "提供自助打印、复印服务"))
                            facilities.add(Facility("储物柜", "提供临时储物柜存放个人物品"))
                            facilities.add(Facility("饮水机", "免费提供冷热饮用水"))
                            facilities.add(Facility("WiFi覆盖", "全馆高速WiFi覆盖，支持在线学习"))
                            facilities.add(Facility("空调系统", "中央空调，四季恒温舒适"))

                            val location = firstRoom.address.ifBlank { "XX市XX区XX路XX号XX大厦X层" }
                            val hours = firstRoom.openingHours.ifBlank { "周一至周日 08:00 - 23:00（节假日照常营业）" }

                            _state.value = GuideUiState(
                                facilities = facilities,
                                location = location,
                                openingHours = hours,
                                contact = "电话：400-XXX-XXXX\n邮箱：contact@istudyspot.com",
                                isLoading = false
                            )
                        } else {
                            _state.value = mockState()
                        }
                    }
                    is ApiResponse.Error -> {
                        _state.value = mockState()
                    }
                }
            } catch (e: Exception) {
                _state.value = mockState()
            }
        }
    }
}
